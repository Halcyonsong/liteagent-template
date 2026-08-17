# liteagent-memory 使用指南

本文档提供 `liteagent-memory` 所有公共 API 的调用示例。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-memory</artifactId>
    <version>${revision}</version>
</dependency>
```

> `${revision}` 为 `liteagent-parent` POM 中定义的版本属性。若您的项目未继承 `liteagent-parent`，请替换为实际版本号。

## 1. 创建记忆窗口存储

记忆窗口存储（MemoryWindowStore）是应用级单例，管理多个会话的记忆窗口。

### 1.1 内存存储（默认）

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

class MemoryStoreExample {
    void create() {
        // 方式一：显式创建独立实例
        MemoryWindowStore store1 = MemoryWindows.inMemory();

        // 方式二：全局懒加载单例
        MemoryWindowStore store2 = MemoryWindows.shared();
    }
}
```

两种方式的区别：

| 方式 | 场景 | 隔离性 |
|---|---|---|
| `inMemory()` | 常规场景，Spring bean | 每个实例独立 Map |
| `shared()` | 不想管生命周期 | 全局共享一个 Map |

### 1.2 持久化存储（开发者自行实现）

继承 `InMemoryMemoryWindowStore`，覆写 `loadHistory` 和 `persist`：

```java
import io.github.halcyonsong.liteagent.memory.window.impl.InMemoryMemoryWindowStore;
import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;

public class JdbcMemoryWindowStore extends InMemoryMemoryWindowStore {

    private final MessageRepository repository;
    private final int windowSize;

    public JdbcMemoryWindowStore(MessageRepository repository, int windowSize) {
        this.repository = repository;
        this.windowSize = windowSize;
    }

    @Override
    public List<Message> loadHistory(String sessionId) {
        // 启用 sessionId 时若内存无窗口，从数据库加载最近 N 条
        return repository.findRecent(sessionId, windowSize);
    }

    @Override
    public void persist(String sessionId, List<Message> messages) {
        // 框架不自动调用，开发者显式调用时写回数据库
        repository.replace(sessionId, messages);
    }
}
```

> `MessageRepository` 为开发者自行实现的 DAO 接口，此处仅作示例。

`getOrCreate` 内部会在首次访问时调用 `loadHistory` 加载历史填充内存窗口，后续访问直接返回内存窗口。

## 2. 创建 Hook

Hook 在 agent 构造时注入，负责在编排的特定时机加载历史和写回本轮。

### 2.1 Chat 侧

```java
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

class ChatHookExample {
    void create(MemoryWindowStore store) {
        // 指定窗口上限
        MemoryChatStepHook hook1 = new MemoryChatStepHook(store, 100);

        // 默认上限 40
        MemoryChatStepHook hook2 = new MemoryChatStepHook(store);
    }
}
```

### 2.2 Stream 侧

```java
import io.github.halcyonsong.liteagent.memory.hook.stream.MemoryStreamStepHook;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

class StreamHookExample {
    void create(MemoryWindowStore store) {
        // 指定窗口上限
        MemoryStreamStepHook hook1 = new MemoryStreamStepHook(store, 100);

        // 默认上限 100
        MemoryStreamStepHook hook2 = new MemoryStreamStepHook(store);
    }
}
```

### 2.3 自定义步骤触发

如果步骤链有自定义 key，继承重写匹配方法：

```java
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

public class CustomMemoryChatStepHook extends MemoryChatStepHook {

    public CustomMemoryChatStepHook(MemoryWindowStore store, int maxSize) {
        super(store, maxSize);
    }

    @Override
    protected boolean shouldLoadHistory(ChatStepKey key) {
        return "MY_CUSTOM_INIT".equals(key.name());
    }

    @Override
    protected boolean shouldSaveRound(ChatStepKey key) {
        return "MY_CUSTOM_END".equals(key.name());
    }
}
```

## 3. 构造请求（携带 sessionId）

sessionId 是记忆窗口的会话标识，放在 `ChatRequest` 中随请求传递。

### 3.1 完整自定义请求

```java
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;

class ChatRequestExample {
    void build() {
        ChatRequest chatRequest = ChatRequest.builder()
                .sessionId("session-123")
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好"))
                .build();
    }
}
```

### 3.2 快捷请求

```java
import io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest.OpenAiQuickChatRequest;

class QuickRequestExample {
    void build(String baseUrl, String apiKey, String model) {
        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .sessionId("session-123")
                .userMessage("你好")
                .build();
    }
}
```

### 3.3 无记忆降级

sessionId 为 null 时，hook 直接跳过，不读写窗口，行为与无记忆完全一致：

```java
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;

class NoMemoryExample {
    void build() {
        ChatRequest chatRequest = ChatRequest.builder()
                // 不设置 sessionId
                .addMessage(Messages.user("你好"))
                .build();
    }
}
```

## 4. Chat 场景完整示例

### 4.1 普通 Java

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;

import java.util.List;

public class ChatMemoryExample {

    public void run(HttpRuntimeConfig runtimeConfig, OpenAiBaseRequest baseRequest) {
        // 1. 创建存储
        MemoryWindowStore store = MemoryWindows.inMemory();

        // 2. 创建 agent，注入 hook
        OpenAiChatAgent agent = OpenAiChatAgents.create(
                runtimeConfig,
                List.of(new MemoryChatStepHook(store, 100)),
                1000,
                10
        );

        // 3. 构造请求
        ChatRequest chatRequest = ChatRequest.builder()
                .sessionId("session-123")
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build();

        // 4. 执行
        OpenAiChatCompletionResponse response = agent.execute(request);

        // 5. 持久化（可选，仅持久化场景）
        MemoryWindow window = store.getOrCreate("session-123");
        store.persist("session-123", window.messages());
    }
}
```

### 4.2 Spring

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

@Configuration
class MemoryConfig {
    @Bean
    MemoryWindowStore memoryWindowStore() {
        return MemoryWindows.inMemory();
    }
}

@Service
class ChatService {

    @Autowired MemoryWindowStore store;
    @Autowired HttpRuntimeConfig runtimeConfig;
    @Autowired OpenAiBaseRequest baseRequest;

    private OpenAiChatAgent agent;

    @PostConstruct
    void init() {
        agent = OpenAiChatAgents.create(
                runtimeConfig,
                List.of(new MemoryChatStepHook(store, 100)),
                1000,
                10
        );
    }

    public String chat(String sessionId, String userMessage) {
        ChatRequest chatRequest = ChatRequest.builder()
                .sessionId(sessionId)
                .addMessage(Messages.user(userMessage))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build();

        return agent.execute(request).getChoices().get(0).getMessage().getContent();
    }
}
```

## 5. Stream 场景完整示例

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.hook.stream.MemoryStreamStepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.provider.openai.support.Printers;

import java.util.List;

public class StreamMemoryExample {

    public void run(HttpRuntimeConfig streamRuntimeConfig, OpenAiBaseRequest baseRequest) {
        MemoryWindowStore store = MemoryWindows.inMemory();

        OpenAiStreamAgent agent = OpenAiStreamAgents.create(
                streamRuntimeConfig,
                List.of(new MemoryStreamStepHook(store, 100)),
                1000,
                10
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .sessionId("session-123")
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build();

        agent.execute(request)
                .doOnNext(Printers::printStreamDeltaAll)
                .blockLast();
    }
}
```

Stream 侧的 hook 时机与 Chat 侧完全对称：`afterStep(INIT_WORKING_MESSAGES)` 加载历史，`beforeStep(END)` 写回。

边界场景：Stream 侧下游 `cancel` 时 END 不一定触发，本轮消息可能不写回。这是可接受语义——取消意味着用户主动中断，本轮消息不完整。

## 6. 持久化场景完整示例

```java
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

import java.util.List;

public class PersistMemoryExample {

    public void run(HttpRuntimeConfig runtimeConfig, OpenAiChatCompletionRequest request,
                    MessageRepository repository) {
        // 1. 创建持久化存储
        MemoryWindowStore store = new JdbcMemoryWindowStore(repository, 100);

        // 2. 创建 agent
        OpenAiChatAgent agent = OpenAiChatAgents.create(
                runtimeConfig,
                List.of(new MemoryChatStepHook(store, 100)),
                1000,
                10
        );

        // 3. 执行
        OpenAiChatCompletionResponse response = agent.execute(request);

        // 4. 显式持久化
        MemoryWindow window = store.getOrCreate("session-123");
        store.persist("session-123", window.messages());
    }
}
```

工作流：

```text
首次请求 (session-123):
  getOrCreate("session-123")
    → 内存无窗口
    → loadHistory("session-123") 从数据库读最近 100 条
    → 创建内存窗口，填充历史
  编排执行
  saveRound: 折叠写回内存窗口
  store.persist(...): 写回数据库

后续请求 (session-123):
  getOrCreate("session-123")
    → 内存已有窗口，直接返回
  编排执行
  saveRound: 折叠写回内存窗口
  store.persist(...): 写回数据库

进程重启后首次请求 (session-123):
  getOrCreate("session-123")
    → 内存无窗口（重启后 Map 清空）
    → loadHistory("session-123") 从数据库读最近 100 条
    → 创建内存窗口，填充历史
  编排执行
  ...
```

## 7. MemoryWindow 接口

记忆窗口是单个会话的消息存储，提供四类操作：

```java
import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;
import java.util.Optional;

public interface MemoryWindow {

    String sessionId();

    // 查看（不移除）
    Optional<Message> peekEarliest();
    Optional<Message> peekLatest();

    // 取出全部（窗口清空）
    List<Message> messages();

    // 取出（移除并返回）
    Optional<Message> pollEarliest();
    Optional<Message> pollLatest();

    // 移除（不返回）
    void removeEarliest();
    void removeLatest();
    void clear();

    // 追加（尾部）
    void append(Message message);
    void appendAll(List<? extends Message> messages);

    // 元信息
    int size();

    default boolean isEmpty() {
        return size() == 0;
    }
}
```

### 操作语义

| 类别 | 方法 | 行为 |
|---|---|---|
| 查看 | `peekEarliest` / `peekLatest` | 只看不移除 |
| 取出 | `messages()` | 取出全部，窗口清空 |
| 取出 | `pollEarliest` / `pollLatest` | 移除并返回 |
| 移除 | `removeEarliest` / `removeLatest` / `clear` | 只移除不返回 |
| 追加 | `append` / `appendAll` | 加到尾部 |

`messages()` 是取出语义：调用后窗口清空。框架内部 `loadHistory` 用它取出历史，`saveRound` 用 `appendAll` 写回完整快照。开发者调用 `store.persist` 时也用 `messages()` 取出快照持久化。

## 8. 多会话隔离

同一个 store 可以管理多个会话，互不干扰：

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;

class MultiSessionExample {
    void demo() {
        MemoryWindowStore store = MemoryWindows.inMemory();

        // 会话 A
        MemoryWindow windowA = store.getOrCreate("session-a");
        windowA.append(new UserMessage("A 的消息"));

        // 会话 B
        MemoryWindow windowB = store.getOrCreate("session-b");
        windowB.append(new UserMessage("B 的消息"));

        // 两个窗口完全独立
        assert windowA.sessionId().equals("session-a");
        assert windowB.sessionId().equals("session-b");
        assert windowA.size() == 1;
        assert windowB.size() == 1;
    }
}
```

如果需要多实例隔离（如多租户），创建多个 store：

```java
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

class MultiTenantExample {
    void demo() {
        MemoryWindowStore store1 = MemoryWindows.inMemory();
        MemoryWindowStore store2 = MemoryWindows.inMemory();

        // 相同 sessionId 在不同 store 里是不同窗口
        MemoryWindow w1 = store1.getOrCreate("session-123");
        MemoryWindow w2 = store2.getOrCreate("session-123");
        assert w1 != w2;
    }
}
```

## 9. 窗口大小控制

窗口大小由两个独立的控制点决定：

| 控制点 | 位置 | 作用 |
|---|---|---|
| 运行时裁剪 | hook 构造时传入的 `maxSize` | 每次 END 时 `while (size > maxSize) removeEarliest()` |
| 启动加载条数 | Store 实现的 `loadHistory` | 从数据库读 `LIMIT windowSize` 条 |

两者应保持一致。如果 hook maxSize=100 但数据库加载了 200 条，第一次请求后裁剪到 100 条，多出的 100 条丢失。

## 10. 消息折叠逻辑

`MemoryHookSupport.foldMessages` 负责折叠消息：

```java
import io.github.halcyonsong.liteagent.memory.hook.support.MemoryHookSupport;
import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;

import java.util.List;

class FoldExample {
    void demo() {
        List<Message> input = List.of(
                new SystemMessage("system"),
                new UserMessage("问题"),
                new AssistantMessage("第一段"),
                new AssistantMessage("第二段")
        );

        List<Message> result = MemoryHookSupport.foldMessages(input);
        // result = [UserMessage("问题"), AssistantMessage("第一段\n第二段")]
    }
}
```

折叠规则：

- `SystemMessage` 跳过
- `ToolMessage` 跳过
- 连续 `AssistantMessage` 合并为一条（content 按顺序拼接，换行分隔）
- `UserMessage` 保留

历史消息已经折叠过，线性扫描不会对它们产生重复合并。

## 11. 直接操作 MemoryWindow

脱离 hook 也可以直接操作窗口：

```java
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;

import java.util.List;
import java.util.Optional;

class DirectWindowExample {
    void demo(MemoryWindowStore store) {
        MemoryWindow window = store.getOrCreate("session-123");

        // 追加消息
        window.append(new UserMessage("你好"));
        window.append(new AssistantMessage("你好，有什么可以帮你的？"));

        // 查看最新消息
        Optional<?> latest = window.peekLatest();

        // 取出全部（窗口清空）
        List<?> all = window.messages();

        // 清空
        window.clear();

        // 删除整个会话
        store.delete("session-123");
    }
}
```
