# liteagent-provider-openai-agent 使用指南

本文档提供 `liteagent-provider-openai-agent` 所有公共 API 的调用示例。

本模块是 OpenAI provider 的编排接入层，在 `liteagent-provider-openai` 协议适配层之上组装可执行的步骤链，支持多轮工具调用。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-provider-openai-agent</artifactId>
    <version>${revision}</version>
</dependency>
```

> `${revision}` 为 `liteagent-parent` POM 中定义的版本属性。若您的项目未继承 `liteagent-parent`，请替换为实际版本号。

## 1. 创建 Chat Agent（同步）

### 最简创建

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

class ChatAgentCreateExample {
    void demo() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder().build();
        OpenAiChatAgent agent = OpenAiChatAgents.create(config);
    }
}
```

### 指定 maxStepCount 和 maxIterations

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

class ChatAgentMaxExample {
    void demo(HttpRuntimeConfig config) {
        // 仅指定 maxStepCount
        OpenAiChatAgent agent1 = OpenAiChatAgents.create(config, 500);

        // 同时指定 maxStepCount 和 maxIterations
        OpenAiChatAgent agent2 = OpenAiChatAgents.create(config, 500, 5);
    }
}
```

### 传入 StepHook

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;

import java.util.List;

class ChatAgentHookExample {
    void demo(HttpRuntimeConfig config) {
        StepHook logHook = new StepHook() {
            @Override
            public void beforeStep(ChatStepKey key, ChatAgentContext context) {
                System.out.println("[before] " + key.name());
            }

            @Override
            public void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
                System.out.println("[after] " + key.name() + " -> " + nextKey.name());
            }
        };

        OpenAiChatAgent agent = OpenAiChatAgents.create(config, List.of(logHook), 500, 5);
    }
}
```

### 直接传入 WebClient

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import org.springframework.web.reactive.function.client.WebClient;

class ChatAgentWebClientExample {
    void demo() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.siliconflow.cn")
                .build();

        OpenAiChatAgent agent1 = OpenAiChatAgents.create(webClient);
        OpenAiChatAgent agent2 = OpenAiChatAgents.create(webClient, 500, 5);
    }
}
```

### 自定义 WebClientRegistry

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;

import java.util.List;

class ChatAgentRegistryExample {
    void demo(HttpRuntimeConfig config) {
        WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());
        OpenAiChatAgent agent = OpenAiChatAgents.create(registry, config, List.of(), 500, 5);
    }
}
```

## 2. 执行 Chat Agent

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

class ExecuteChatExample {
    void demo(OpenAiChatAgent agent) {
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("sk-xxx")
                        .model("Qwen/Qwen2.5-7B-Instruct")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("你好"))
                        .build())
                .build();

        // 方式一：直接获取最终响应
        OpenAiChatCompletionResponse response = agent.execute(request);
        String content = response.getChoices().get(0).getMessage().getContent();

        // 方式二：获取完整上下文（含中间态、终止原因）
        ChatAgentContext context = agent.executeContext(request);
        OpenAiChatCompletionResponse response2 = (OpenAiChatCompletionResponse) context.getResult();
        AgentTerminationReason reason = context.getTerminationReason();
    }
}
```

## 3. 创建 Stream Agent（流式）

创建方式与 Chat Agent 完全对称：

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;

import java.util.List;

class StreamAgentCreateExample {
    void demo(HttpRuntimeConfig config, WebClient webClient, StreamStepHook streamHook) {
        // 最简创建
        OpenAiStreamAgent agent1 = OpenAiStreamAgents.create(config);

        // 指定 maxStepCount 和 maxIterations
        OpenAiStreamAgent agent2 = OpenAiStreamAgents.create(config, 500, 5);

        // 传入 Hook
        OpenAiStreamAgent agent3 = OpenAiStreamAgents.create(config, List.of(streamHook), 500, 5);

        // 直接传入 WebClient
        OpenAiStreamAgent agent4 = OpenAiStreamAgents.create(webClient);
    }
}
```

## 4. 执行 Stream Agent

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import reactor.core.publisher.Flux;

import java.util.List;

class ExecuteStreamExample {
    void demo(OpenAiStreamAgent agent, OpenAiChatCompletionRequest request) {
        // 方式一：直接获取输出流
        Flux<OpenAiStreamCompletionResponse> stream = agent.execute(request);
        stream.doOnNext(chunk -> {
                    String delta = chunk.getChoices().get(0).getDelta().getContent();
                    if (delta != null) {
                        System.out.print(delta);
                    }
                })
                .blockLast();

        // 方式二：获取完整上下文
        StreamAgentContext<OpenAiStreamCompletionResponse> context = agent.executeContext(request);
        Flux<OpenAiStreamCompletionResponse> output = context.getOutput();
        List<StreamRoundState> rounds = context.getRounds();
    }
}
```

## 5. 工具调用

### 定义工具

```java
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;

@ToolComponent
public class WeatherTools {

    @ToolMethod(name = "get_weather", description = "获取指定城市的天气")
    public String getWeather(
            @ToolParam(description = "城市名") String city
    ) {
        return city + " 晴 32度";
    }
}
```

### 注册工具到请求

```java
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

class RegisterToolsExample {
    void demo(OpenAiChatAgent agent, OpenAiBaseRequest baseRequest) {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());
        OpenAiRegistryToolsAdvisor toolsAdvisor = new OpenAiRegistryToolsAdvisor(registry);

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .requestAdvisor(toolsAdvisor)
                .build();

        // 执行时自动多轮工具调用
        OpenAiChatCompletionResponse response = agent.execute(request);
    }
}
```

### 指定 tool_choice

```java
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiToolChoiceAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

class ToolChoiceExample {
    void demo(OpenAiBaseRequest baseRequest) {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());
        OpenAiRegistryToolsAdvisor toolsAdvisor = new OpenAiRegistryToolsAdvisor(registry);

        // 必须调用指定函数
        OpenAiToolChoiceAdvisor choiceAdvisor = new OpenAiToolChoiceAdvisor(
                OpenAiToolChoice.function("get_weather")
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .requestAdvisor(toolsAdvisor)
                .requestAdvisor(choiceAdvisor)
                .build();
    }
}
```

## 6. QuickRequest 快速构造

```java
import io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

class QuickRequestExample {
    void demo(OpenAiChatAgent agent) {
        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey("sk-xxx")
                .model("Qwen/Qwen2.5-7B-Instruct")
                .systemMessage("你是一位助手")
                .userMessage("你好")
                .build();

        // 转换为标准请求后执行
        OpenAiChatCompletionResponse response = agent.execute(quick.toChatCompletion());
    }
}
```

## 7. 快捷请求 + 工具调用完整示例

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

public class QuickRequestWithToolsExample {

    public void run() {
        // 1. 创建工具注册表
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        // 2. 创建 agent
        HttpRuntimeConfig config = HttpRuntimeConfig.builder().build();
        OpenAiChatAgent agent = OpenAiChatAgents.create(config, 500, 5);

        // 3. 构造带工具的请求
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("sk-xxx")
                        .model("Qwen/Qwen2.5-7B-Instruct")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("北京天气怎么样？"))
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(registry))
                .build();

        // 4. 执行（自动多轮工具调用）
        OpenAiChatCompletionResponse response = agent.execute(request);

        // 5. 读取最终结果
        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println(content);  // 例如："北京 晴 32度"
    }
}
```

## 8. 流式 + 工具调用完整示例

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

public class StreamWithToolsExample {

    public void run() {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        HttpRuntimeConfig config = HttpRuntimeConfig.builder().build();
        OpenAiStreamAgent agent = OpenAiStreamAgents.create(config, 500, 5);

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("sk-xxx")
                        .model("Qwen/Qwen2.5-7B-Instruct")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("北京天气怎么样？"))
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(registry))
                .build();

        agent.execute(request)
                .doOnNext(chunk -> {
                    String delta = chunk.getChoices().get(0).getDelta().getContent();
                    if (delta != null) {
                        System.out.print(delta);
                    }
                })
                .blockLast();
    }
}
```

## 9. 参数默认值

| 参数 | 默认值 | 说明 |
|---|---|---|
| `maxStepCount` | 1000 | 单次执行最大步骤数 |
| `maxIterations` | 10 | 最大模型调用轮次（工具调用回环上限） |
| `hooks` | `List.of()` | 步骤钩子列表 |

不传任何参数时使用以上默认值。可通过 `create` 重载方法逐级指定。
