# OpenAI-compatible Chat

本文档说明当前 `liteagent-provider-openai` 模块的几种典型调用方式。

## 1. 统一普通调用方式

这是当前最推荐的普通调用方式。

优点：

- 使用 core 统一模型
- 对调用方最稳定
- 便于后续切换其他 provider

示例：

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;

public class UnifiedChatExample {

    public ChatResult execute(OpenAiChatClient chatClient) {
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(256)
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("your-api-key")
                        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                        .build())
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        return chatClient.chat(invocation);
    }
}
```

## 2. 统一流式调用方式

流式调用不通过 `ChatOptions` 中的参数切换，而是使用独立客户端：

同时，流式请求的运行时超时与普通请求分离：

- `responseTimeoutMillis`：普通请求使用
- `streamResponseTimeoutMillis`：流式请求使用
- 当 `streamResponseTimeoutMillis = null` 时，表示不设置流式总响应超时

```java
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;

public class UnifiedStreamExample {

    public void execute(OpenAiStreamClient streamClient, ChatInvocation invocation) {
        streamClient.stream(invocation)
                .doOnNext(chunk -> {
                    chunk.getChoices().forEach(choice -> {
                        if (choice.getDelta() != null) {
                            System.out.println("role = " + choice.getDelta().getRole());
                            System.out.println("content = " + choice.getDelta().getContent());
                            System.out.println("reasoning = " + choice.getDelta().getReasoningContent());
                        }
                    });
                })
                .blockLast();
    }
}
```

## 3. 直接使用 provider 请求对象

如果你需要传入 OpenAI-compatible 扩展参数，可以直接使用 provider 请求包装对象。

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

public class ProviderRequestExample {

    public OpenAiChatCompletionResponse execute(OpenAiChatClient chatClient,
                                                ChatRequest chatRequest,
                                                ChatOptions chatOptions) {
        OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
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
                .chatOptions(chatOptions)
                .completionOptions(completionOptions)
                .build();

        return chatClient.chat(request);
    }
}
```

## 4. 使用 QuickRequest 快速调用

适合：

- 快速测试
- 最短接入
- 示例场景

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

public class QuickRequestExample {

    public OpenAiChatCompletionResponse execute(OpenAiChatClient chatClient) {
        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey("your-api-key")
                .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                .systemMessage("You are a helpful assistant.")
                .userMessage("你好，请简单介绍一下你自己。")
                .build();

        return chatClient.chatCompletion(request);
    }
}
```

## 5. 读取 provider 扩展普通响应字段

当前普通 provider 响应已保留：

- `reasoning_content`
- `tool_calls`
- provider 扩展 usage 字段

如果你需要读取这些字段，应获取 `OpenAiChatCompletionResponse`。

```java
import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiToolCall;

public class ProviderResponseReader {

    public void print(OpenAiChatCompletionResponse response) {
        for (ChatChoice choice : response.getChoices()) {
            for (Message message : choice.getChatResponse().getMessages()) {
                System.out.println("content = " + message.getContent());

                if (message instanceof OpenAiAssistantMessage openAiMessage) {
                    System.out.println("reasoning = " + openAiMessage.getReasoningContent());

                    for (OpenAiToolCall toolCall : openAiMessage.getToolCalls()) {
                        System.out.println("tool call id = " + toolCall.getId());
                        System.out.println("tool call type = " + toolCall.getType());

                        if (toolCall.getFunction() != null) {
                            System.out.println("function name = " + toolCall.getFunction().getName());
                            System.out.println("function arguments = " + toolCall.getFunction().getArguments());
                        }
                    }
                }
            }
        }
    }
}
```

## 6. 读取 provider 流式响应字段

流式 provider 响应当前返回：

- `OpenAiStreamCompletionResponse`
- 内部复用 core `StreamChoice` / `StreamDelta`
- 额外保留 provider 顶层 usage 结构

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

public class ProviderStreamReader {

    public void print(OpenAiStreamClient streamClient, ChatInvocation invocation) {
        streamClient.streamCompletion(invocation)
                .doOnNext(response -> {
                    System.out.println("response id = " + response.getBaseResponse().getId());
                    response.getChoices().forEach(choice -> {
                        if (choice.getDelta() != null) {
                            System.out.println("delta role = " + choice.getDelta().getRole());
                            System.out.println("delta content = " + choice.getDelta().getContent());
                            System.out.println("delta reasoning = " + choice.getDelta().getReasoningContent());
                        }
                    });

                    if (response.getUsage() != null) {
                        System.out.println("prompt tokens = " + response.getUsage().getPromptTokens());
                        System.out.println("completion tokens = " + response.getUsage().getCompletionTokens());
                        System.out.println("total tokens = " + response.getUsage().getTotalTokens());
                        System.out.println("prompt cache hit tokens = " + response.getUsage().getPromptCacheHitTokens());
                        System.out.println("prompt cache miss tokens = " + response.getUsage().getPromptCacheMissTokens());
                    }
                })
                .blockLast();
    }
}
```

## 7. 统一结果与 provider 结果的区别

### 普通统一结果 `ChatResult`

特点：

- 面向统一抽象
- 不暴露 provider 特有字段
- 更适合上层业务稳定依赖

### 普通 provider 结果 `OpenAiChatCompletionResponse`

特点：

- 保留 `reasoning_content`
- 保留 `tool_calls`
- 保留 provider usage 扩展字段

### 流式统一结果 `StreamChunk`

特点：

- 面向统一流式抽象
- 仅保留统一 delta 结构
- 更适合上层稳定消费

### 流式 provider 结果 `OpenAiStreamCompletionResponse`

特点：

- 面向 OpenAI-compatible 流式语义
- 保留 provider 顶层 usage 扩展字段
- 更适合需要读取供应商扩展信息时使用

## 8. example 模块配置方式

当前建议通过 `liteagent-examples` 模块进行本地联调。

推荐配置模式：

- `application.yaml`：模板主配置
- `application-local.yaml`：本地私有配置

主配置中通过：

```yaml
spring:
  config:
    import: optional:classpath:application-local.yaml
```

导入副配置文件。

这样主配置可提交仓库作为模板，而真实 key 放在本地副配置文件中。

## 9. 当前限制

当前本文档对应能力包括：

- 普通非流式对话调用
- 流式对话调用
- OpenAI-compatible provider 扩展字段读取

暂未覆盖：

- tools 完整闭环
- 多模态
- agent 编排

如果示例工程需要显式区分普通与流式超时，建议配置为：

```yaml
liteagent:
  openai:
    runtime:
      max-in-memory-size: 16777216
      connect-timeout-millis: 5000
      response-timeout-millis: 60000
      stream-response-timeout-millis: null
```
