# liteagent-agent 使用指南

本文档提供 `liteagent-agent` 所有公共 API 的调用示例。

`liteagent-agent` 是纯编排层，不包含 provider 实现。以下示例中的 `ChatStep` / `StreamStep` 实现均为测试桩，实际使用时由 provider-agent 模块提供具体步骤实现。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-agent</artifactId>
    <version>${revision}</version>
</dependency>
```

> `${revision}` 为 `liteagent-parent` POM 中定义的版本属性。若您的项目未继承 `liteagent-parent`，请替换为实际版本号。

## 1. ChatAgent 基本用法

`ChatAgent` 是同步编排入口，提供两个方法：

```java
import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.Result;

class ChatAgentExample {
    void demo(ChatAgent agent, Invocation invocation) {
        // 方式一：只拿最终结果
        Result result = agent.execute(invocation);

        // 方式二：拿完整上下文（含中间态、终止原因、attributes）
        ChatAgentContext context = agent.executeContext(invocation);
        Result result2 = context.getResult();
    }
}
```

`execute()` 适合只需要最终响应的场景；`executeContext()` 适合需要调试中间步骤、读取 `terminationReason` 或访问 `attributes` 扩展数据的场景。

## 2. 构造 ChatAgentExecutor

```java
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChatExecutorExample {
    void demo() {
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, ctx -> ChatStepKey.MAP_REQUEST);
        steps.put(ChatStepKey.MAP_REQUEST, ctx -> ChatStepKey.BUILD_RESULT);
        steps.put(ChatStepKey.BUILD_RESULT, ctx -> ChatStepKey.END);
        steps.put(ChatStepKey.END, ctx -> ChatStepKey.END);

        // 最简构造
        ChatAgentExecutor executor1 = new ChatAgentExecutor(steps);

        // 带 hook 和最大步骤数
        ChatAgentExecutor executor2 = new ChatAgentExecutor(steps, List.of(), 1000);

        // 带 hook、最大步骤数和最大迭代轮次
        ChatAgentExecutor executor3 = new ChatAgentExecutor(steps, List.of(), 1000, 10);
    }
}
```

构造器参数说明：

| 参数 | 默认值 | 说明 |
|---|---|---|
| `steps` | 必填 | 步骤注册表 |
| `hooks` | `List.of()` | 步骤生命周期钩子列表 |
| `maxStepCount` | 1000 | 单次执行最大步骤数（防止无限循环） |
| `maxIterations` | 10 | 最大模型调用轮次（防止无限工具调用循环） |

## 3. StreamAgent 基本用法

`StreamAgent<T>` 是流式编排入口，泛型 `T` 是 provider 输出的单个流元素类型：

```java
import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import reactor.core.publisher.Flux;

class StreamAgentExample {
    void demo(StreamAgent<String> agent, Invocation invocation) {
        // 方式一：直接获取输出流（业务侧首选）
        Flux<String> stream = agent.execute(invocation);
        stream.doOnNext(chunk -> System.out.println(chunk))
              .blockLast();

        // 方式二：获取完整上下文（含输出流、轮次状态等）
        StreamAgentContext<String> context = agent.executeContext(invocation);
        Flux<String> output = context.getOutput();
    }
}
```

## 4. 构造 StreamAgentExecutor

```java
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StreamExecutorExample {
    void demo() {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> StreamStepKey.BUILD_RESULT);
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> StreamStepKey.END);
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) ->
                new StreamApplyResult<>(Flux.just("chunk1", "chunk2"), StreamStepKey.STREAM_END));

        // 最简构造
        StreamAgentExecutor<String> executor1 = new StreamAgentExecutor<>(syncSteps, streamSteps);

        // 带 hook 和最大步骤数
        StreamAgentExecutor<String> executor2 = new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(), 1000);

        // 带 hook、最大步骤数和最大迭代轮次
        StreamAgentExecutor<String> executor3 = new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(), 1000, 10);
    }
}
```

## 5. Step Hook 用法

### Chat StepHook

```java
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ChatHookExample {
    void demo() {
        StepHook logHook = new StepHook() {
            @Override
            public void beforeStep(ChatStepKey key, ChatAgentContext context) {
                System.out.println("[before] " + key.name());
            }

            @Override
            public void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
                System.out.println("[after] " + key.name() + " -> " + nextKey.name());
            }

            @Override
            public void onStepError(ChatStepKey key, ChatAgentContext context, Throwable error) {
                System.err.println("[error] " + key.name() + ": " + error.getMessage());
            }
        };

        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        ChatAgentExecutor executor = new ChatAgentExecutor(steps, List.of(logHook), 1000, 10);
    }
}
```

### Stream StepHook

```java
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StreamHookExample {
    void demo() {
        StreamStepHook logHook = new StreamStepHook() {
            @Override
            public void beforeStep(StreamStepKey key, StreamAgentContext<?> context) {
                System.out.println("[before] " + key.name());
            }

            @Override
            public void afterStep(StreamStepKey key, StreamAgentContext<?> context, StreamStepKey nextKey) {
                System.out.println("[after] " + key.name() + " -> " + nextKey.name());
            }

            @Override
            public void onStepError(StreamStepKey key, StreamAgentContext<?> context, Throwable error) {
                System.err.println("[error] " + key.name() + ": " + error.getMessage());
            }
        };

        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(
                syncSteps, streamSteps, List.of(logHook), 1000, 10);
    }
}
```

Hook 的三个回调时机：

| 回调 | 触发时机 | 用途 |
|---|---|---|
| `beforeStep` | 步骤执行前 | 日志、metrics、trace 起点 |
| `afterStep` | 步骤执行成功后 | 日志、metrics、trace 终点 |
| `onStepError` | 步骤抛出异常时 | 错误记录、告警 |

## 6. maxIterations 配置

`maxIterations` 控制工具调用循环的最大轮次，防止模型反复调用工具导致无限循环：

```java
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MaxIterationsExample {
    void demo() {
        Map<ChatStepKey, ChatStep> chatSteps = new HashMap<>();
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();

        // chat：通过构造器指定
        ChatAgentExecutor executor1 = new ChatAgentExecutor(chatSteps, List.of(), 1000, 5);

        // stream：通过构造器指定
        StreamAgentExecutor<String> executor2 =
                new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(), 1000, 5);
    }
}
```

执行器在 `execute` 时会将 `maxIterations` 写入 context，步骤实现可通过 `context.getMaxIterations()` 读取，并在每次迭代时与 `context.getIteration()` 比较。

## 7. 自定义 Step Key

`ChatStepKey` 和 `StreamStepKey` 均为接口，支持通过 `of(String)` 创建自定义 key：

```java
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;

import java.util.HashMap;
import java.util.Map;

class CustomStepKeyExample {
    void demo() {
        // 创建自定义 key
        ChatStepKey CUSTOM_STEP = ChatStepKey.of("CUSTOM_STEP");

        // 注册到步骤表
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, ctx -> CUSTOM_STEP);
        steps.put(CUSTOM_STEP, ctx -> {
            // 自定义逻辑
            return ChatStepKey.END;
        });
        steps.put(ChatStepKey.END, ctx -> ChatStepKey.END);

        ChatAgentExecutor executor = new ChatAgentExecutor(steps);
    }
}
```

Stream 侧同理：

```java
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

import java.util.HashMap;
import java.util.Map;

class CustomStreamStepKeyExample {
    void demo() {
        StreamStepKey CUSTOM_SYNC = StreamStepKey.of("CUSTOM_SYNC");

        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> CUSTOM_SYNC);
        syncSteps.put(CUSTOM_SYNC, ctx -> StreamStepKey.SEND_REQUEST);
    }
}
```

自定义 key 与内置 key 基于 `name()` 做相等性判断，两个 `of("CUSTOM")` 创建的 key 视为相等。

## 8. Context 扩展数据

两个 Context 都提供 `attributes` 扩展数据槽，用于跨步骤共享自定义数据：

```java
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

class ContextAttributeExample {
    void demo(Invocation invocation) {
        // chat
        ChatAgentContext context = ChatAgentContext.create(invocation);
        context.setAttribute("requestId", "req-001");
        context.setAttribute("retryCount", 0);

        String requestId = context.getAttribute("requestId");
        Integer retryCount = context.getAttribute("retryCount", Integer.class);

        // 传 null 删除
        context.setAttribute("requestId", null);

        // stream
        StreamAgentContext<String> streamContext = StreamAgentContext.create(invocation);
        streamContext.setAttribute("sessionId", "session-abc");
        String sessionId = streamContext.getAttribute("sessionId", String.class);
    }
}
```

## 9. Context 取消机制

两个 Context 都支持中途取消：

```java
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

class ContextCancelExample {
    void demo(Invocation invocation) {
        // chat：在 hook 或步骤中调用
        ChatAgentContext context = ChatAgentContext.create(invocation);
        context.cancel();

        // 执行器在每次步骤前检查 cancelled 标志
        // 如果已取消，设置 terminationReason = CANCELLED 并退出循环

        // stream：同样支持
        StreamAgentContext<String> streamContext = StreamAgentContext.create(invocation);
        streamContext.cancel();

        // 流式执行器在 buildRound 开始时检查 cancelled
        // 同时通过 doOnCancel 处理下游订阅取消
    }
}
```

`cancelled` 字段声明为 `volatile`，确保跨线程可见性。

## 10. 读取终止原因

```java
import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

class TerminationReasonExample {
    void demo(ChatAgent chatAgent, StreamAgent<String> streamAgent, Invocation invocation) {
        // chat
        ChatAgentContext context = chatAgent.executeContext(invocation);
        AgentTerminationReason reason = context.getTerminationReason();

        // stream
        StreamAgentContext<String> streamContext = streamAgent.executeContext(invocation);
        AgentTerminationReason streamReason = streamContext.getTerminationReason();
    }
}
```

终止原因枚举值：

| 值 | 说明 |
|---|---|
| `COMPLETED` | 正常完成 |
| `MAX_ITERATIONS_REACHED` | 达到最大执行轮次 |
| `TOOL_ERROR` | 工具执行失败 |
| `MODEL_ERROR` | 模型调用失败 |
| `CANCELLED` | 被外部主动取消 |

## 11. Stream 轮次状态

流式执行中，每一轮的运行时状态通过 `StreamRoundState` 维护：

```java
import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

import java.util.List;

class StreamRoundExample {
    void demo(StreamAgent<String> agent, Invocation invocation) {
        StreamAgentContext<String> context = agent.executeContext(invocation);

        // 获取所有轮次
        List<StreamRoundState> rounds = context.getRounds();

        // 获取当前轮
        StreamRoundState current = context.currentRound();

        // 获取指定轮
        StreamRoundState round0 = context.getRound(0);

        // 读取轮次数据
        int roundIndex = current.getRoundIndex();
        boolean complete = current.isRoundComplete();
        Object accumulator = current.getAccumulator();
        Object finalResponse = current.getFinalResponse();

        // 读取轮次扩展属性
        Object toolCalls = current.getAttribute("toolCalls");
    }
}
```
