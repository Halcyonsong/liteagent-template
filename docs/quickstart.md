# 推荐调用方式

本页面面向普通 Java 项目，展示同步 Chat Agent、工具调用和会话记忆组合后的推荐写法。Agent、工具注册表与记忆 Store 应在应用初始化时创建并复用；`ChatRequest` 与 `OpenAiChatCompletionRequest` 应按每次请求创建。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-provider-openai-agent</artifactId>
    <version>${revision}</version>
</dependency>

<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-memory</artifactId>
    <version>${revision}</version>
</dependency>
```

> `${revision}` 为 `liteagent-parent` POM 中定义的版本属性。未继承父 POM 时请替换为实际发布版本。

## 定义工具

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
        return city + " 晴，32 度";
    }
}
```

## 初始化应用组件

工具注册表仅需反射注册一次。默认记忆 Hook 使用全局共享的内存 Store，适合不需要自定义生命周期的普通 Java 场景。

```java
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.memory.hook.MemoryHooks;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

public class CustomerServiceAgent {

    private final ToolRegistry toolRegistry = ToolRegistries.inMemory(
            new WeatherTools()
    );

    private final OpenAiChatAgent agent = OpenAiChatAgents.builder()
            .runtimeConfig(HttpRuntimeConfig.builder().build())
            .addHook(MemoryHooks.chat(100))
            .maxIterations(5)
            .build();

    // 每次请求创建新的 OpenAiChatCompletionRequest
}
```

如需隔离 Store 生命周期或接入数据库持久化，创建自定义 `MemoryWindowStore` 后改为 `.addHook(MemoryHooks.chat(store, 100))`。持久化由业务代码在合适时机显式调用 `store.persist(...)`。

## 执行一次对话

工具注册表通过 `OpenAiRegistryToolsAdvisor` 附加到请求。它会在映射阶段将工具描述注入 OpenAI 请求，同时让 Agent 在模型发起工具调用时取得同一注册表执行工具。

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

public OpenAiChatCompletionResponse chat(String sessionId, String message) {
    ChatRequest chatRequest = ChatRequest.builder()
            .sessionId(sessionId)
            .addMessage(Messages.system("你是一位客服助手。"))
            .addMessage(Messages.user(message))
            .build();

    OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
            .baseRequest(OpenAiBaseRequest.builder()
                    .baseUrl("https://api.example.com")
                    .apiKey("sk-xxx")
                    .model("your-model")
                    .build())
            .chatRequest(chatRequest)
            .requestAdvisor(new OpenAiRegistryToolsAdvisor(toolRegistry))
            .build();

    return agent.execute(request);
}
```

多个请求增强器可以连续添加，按添加顺序执行：

```
.requestAdvisor(traceAdvisor)
.requestAdvisor(new OpenAiRegistryToolsAdvisor(toolRegistry))
.requestAdvisor(choiceAdvisor)
```

当自定义增强器需要读取或修改 `rawRequest.tools` 时，应放在 `OpenAiRegistryToolsAdvisor` 后面。

## 流式调用

流式调用仅将 Agent 与记忆 Hook 改为 Stream 对应类型；工具注册和请求构造方式保持一致。

```java
import io.github.halcyonsong.liteagent.memory.hook.MemoryHooks;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import reactor.core.publisher.Flux;

OpenAiStreamAgent streamAgent = OpenAiStreamAgents.builder()
        .runtimeConfig(HttpRuntimeConfig.builder().build())
        .addHook(MemoryHooks.stream(100))
        .maxIterations(5)
        .build();

Flux<?> output = streamAgent.execute(request);
```

## 更多内容

- [核心消息、请求与工具抽象](./liteagent-core.md)
- [通用 Agent 编排与 Hook 生命周期](./liteagent-agent.md)
- [OpenAI-compatible Provider 请求与 Advisor](./liteagent-provider-openai.md)
- [OpenAI Chat 与 Stream Agent 的全部创建方式](./liteagent-provider-openai-agent.md)
- [记忆窗口、Store 与持久化扩展](./liteagent-memory.md)
- [可运行调用示例](../liteagent-examples/README.md)
