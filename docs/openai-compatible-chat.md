# OpenAI-compatible Chat

本文说明 `liteagent-provider-openai` 与 `liteagent-provider-openai-agent` 当前已经支持的调用方式，以及工具注册链路如何接入请求。

## 0. provider 主线流程

```mermaid
flowchart TD
    Begin([Begin]) --> A1[构造 ChatRequest + OpenAiBaseRequest]
    A1 --> A2[组装 OpenAiChatCompletionRequest]
    A2 --> A3[OpenAiChatClient / OpenAiStreamClient]
    A3 --> A4[Request Mapper]
    A4 --> A5[Advisor 增强]
    A5 --> A6[Transport 发送 HTTP]
    A6 --> A7[接收 Raw Response]
    A7 --> A8[Response Mapper]
    A8 --> A9[返回 provider response]
    A9 --> End([End])
```

说明：

- 这是当前 `liteagent-provider-openai` 的主调用链
- 普通与流式共享 request mapper 与 advisor 增强阶段
- 主要差异在 transport 与 response mapper

## 1. agent 编排主线

```mermaid
flowchart TD
    B1[Invocation] --> B2[OpenAiAgent.execute]
    B2 --> B3[AgentExecutor]
    B3 --> B4[BEGIN]
    B4 --> B5[MAP_REQUEST]
    B5 --> B6[ENHANCE_REQUEST]
    B6 --> B7[SEND_CHAT_REQUEST]
    B7 --> B8[MAP_CHAT_RESPONSE]
    B8 --> B9[ANALYZE_RESPONSE]
    B9 --> B10[BUILD_RESULT]
    B10 --> B11[END]
```

说明：

- 当前 `liteagent-provider-openai-agent` 只支持同步 chat 编排
- `ANALYZE_RESPONSE` 之后还没有接入 `EXECUTE_TOOL`
- 后续工具回环会从 `ANALYZE_RESPONSE` 分叉到 `EXECUTE_TOOL -> MAP_REQUEST`

## 2. 普通 provider 调用

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

public class ProviderRequestExample {

    public OpenAiChatCompletionResponse execute(OpenAiChatClient chatClient) {
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请介绍一下你自己。"))
                .build();

        OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
                .temperature(0.7)
                .maxTokens(256)
                .topP(0.9)
                .n(1)
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("your-api-key")
                        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                        .build())
                .chatRequest(chatRequest)
                .completionOptions(completionOptions)
                .build();

        return chatClient.chatCompletion(request);
    }
}
```

## 3. 普通 agent 编排调用

```java
import io.github.halcyonsong.liteagent.provider.openai.agent.OpenAiAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.factory.OpenAiAgents;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

public class ProviderAgentExample {

    public OpenAiChatCompletionResponse execute(OpenAiChatCompletionRequest request) {
        OpenAiAgent agent = OpenAiAgents.create(
                HttpRuntimeConfig.builder()
                        .maxInMemorySize(16 * 1024 * 1024)
                        .connectTimeoutMillis(5000)
                        .responseTimeoutMillis(60000L)
                        .build()
        );

        return agent.execute(request);
    }
}
```

## 4. 流式 provider 调用

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

public class ProviderStreamExample {

    public Flux<OpenAiStreamCompletionResponse> execute(OpenAiStreamClient streamClient,
                                                        OpenAiChatCompletionRequest request) {
        return streamClient.streamCompletion(request);
    }
}
```

## 5. tools 注入链路

当前工具链路是“注册后增强请求”，不是“直接把方法塞进 client”。

### 工具注册阶段

```mermaid
flowchart TD
    T1[工具类 @ToolComponent] --> T2[ReflectionToolRegistrar 扫描 @ToolMethod]
    T2 --> T3[解析 @ToolParam 参数名 / 描述 / required]
    T3 --> T4[构建 JSON Schema type / properties / required]
    T4 --> T5[SimpleToolDefinition]
    T5 --> T6[InMemoryToolRegistry 存储]
```

### 请求增强阶段

```mermaid
flowchart TD
    T6[ToolRegistry.getAll] --> R1[OpenAiToolSpecResolver.resolve]
    R1 --> R2[ToolDefinition 转 OpenAiToolSpec]
    R2 --> R3[OpenAiRegistryToolsAdvisor.enhance]
    R3 --> R4[raw request.tools]
    C1[OpenAiToolChoice] --> C2[OpenAiToolChoiceAdvisor.enhance]
    C2 --> C3[raw request.tool_choice]
```

说明：

- `InMemoryToolRegistry` 只是注册容器，不直接参与请求发送
- `OpenAiToolSpecResolver` 负责将 core 层 `ToolDefinition` 转换为 provider 层 `OpenAiToolSpec`
- `OpenAiRegistryToolsAdvisor` 才是真正把工具注入到请求里的地方
- `OpenAiToolChoiceAdvisor` 负责补充 `tool_choice`
- 当前还没有自动执行工具后的下一轮请求闭环

## 6. 示例配置

建议把主配置当模板，把真实密钥放到副配置里。

```yaml
spring:
  config:
    import: optional:classpath:application-local.yaml

liteagent:
  openai:
    enabled: true
    base-url: ${LITEAGENT_OPENAI_BASE_URL:}
    api-key: ${LITEAGENT_OPENAI_API_KEY:}
    model: ${LITEAGENT_OPENAI_MODEL:}
    runtime:
      max-in-memory-size: 16777216
      connect-timeout-millis: 5000
      response-timeout-millis: 60000
      stream-response-timeout-millis: 300000
```

## 7. 当前限制

目前已经实现的部分：

- 普通 provider chat
- 流式 provider chat
- provider 扩展字段保留
- 工具注册和请求增强
- OpenAI 最小同步 agent 编排骨架

还没有完成的部分：

- 工具执行闭环
- 多轮 agent 编排
- 响应增强器
- 流式 agent 编排
