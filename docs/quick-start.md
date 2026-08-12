# Quick Start

本文档给出 `liteagent-template` 当前第一版最短接入方式。

当前 Quick Start 面向：

- OpenAI-compatible 协议供应商
- Java 17
- 普通对话调用
- 流式对话调用

## 1. 引入依赖

通常至少需要：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.halcyonsong</groupId>
        <artifactId>liteagent-core</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>io.github.halcyonsong</groupId>
        <artifactId>liteagent-provider-openai</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## 2. 创建客户端

### 普通对话客户端

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.factory.OpenAiClients;

public class ChatClientExample {

    public OpenAiChatClient createChatClient() {
        return OpenAiClients.create(
                16 * 1024 * 1024,
                5000,
                60000L
        );
    }
}
```

### 流式对话客户端

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.client.factory.OpenAiClients;

public class StreamClientExample {

    public OpenAiStreamClient createStreamClient() {
        return OpenAiClients.createStream(
                16 * 1024 * 1024,
                5000,
                null
        );
    }
}
```

说明：

- 普通客户端第三个参数是 `responseTimeoutMillis`
- 流式客户端第三个参数是 `streamResponseTimeoutMillis`
- 流式超时传 `null` 表示不设置流式总响应超时

## 3. 构造统一调用请求

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;

public class InvocationExample {

    public ChatInvocation buildInvocation() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey("your-api-key")
                .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(256)
                .build();

        return ChatInvocation.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();
    }
}
```

说明：

- `baseUrl` 只提供基础地址即可，例如：
  - `https://api.siliconflow.cn`
  - `https://api.siliconflow.cn/v1`
  - `https://api.siliconflow.cn/v1/chat/completions`
- 框架会自动规范化到 `/v1/chat/completions`

## 4. 发起普通调用并读取统一结果

```java
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;

public class ChatCallExample {

    public void printChatResult(OpenAiChatClient chatClient, ChatInvocation invocation) {
        ChatResult result = chatClient.chat(invocation);

        System.out.println("response id = " + result.getBaseResponse().getId());
        System.out.println("model = " + result.getBaseResponse().getModel());

        for (ChatChoice choice : result.getChoices()) {
            System.out.println("choice index = " + choice.getIndex());
            System.out.println("finish reason = " + choice.getFinishReason());

            choice.getChatResponse().getMessages().forEach(message ->
                    System.out.println("message content = " + message.getContent())
            );
        }
    }
}
```

## 5. 发起流式调用并读取统一流式结果

```java
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;

public class StreamCallExample {

    public void printStreamResult(OpenAiStreamClient streamClient, ChatInvocation invocation) {
        streamClient.stream(invocation)
                .doOnNext(chunk -> {
                    System.out.println("response id = " + chunk.getBaseResponse().getId());
                    chunk.getChoices().forEach(choice -> {
                        if (choice.getDelta() != null) {
                            System.out.println("delta role = " + choice.getDelta().getRole());
                            System.out.println("delta content = " + choice.getDelta().getContent());
                            System.out.println("delta reasoning = " + choice.getDelta().getReasoningContent());
                        }
                    });
                })
                .blockLast();
    }
}
```

## 6. 什么时候使用 provider 响应而不是统一结果

如果你只关心通用对话内容，优先使用统一结果：

### 普通

```java
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;

public class UnifiedChatExample {

    public ChatResult execute(OpenAiChatClient chatClient, ChatInvocation invocation) {
        return chatClient.chat(invocation);
    }
}
```

### 流式

```java
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChunk;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import reactor.core.publisher.Flux;

public class UnifiedStreamExample {

    public Flux<StreamChunk> execute(OpenAiStreamClient streamClient, ChatInvocation invocation) {
        return streamClient.stream(invocation);
    }
}
```

如果你需要读取 OpenAI-compatible 协议扩展字段，应使用 provider 响应对象：

### 普通 provider 响应

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

public class ProviderChatExample {

    public OpenAiChatCompletionResponse execute(OpenAiChatClient chatClient) {
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请简单介绍一下你自己。"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("your-api-key")
                        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                        .build())
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.7)
                        .maxTokens(256)
                        .build())
                .build();

        return chatClient.chatCompletion(request);
    }
}
```

### 流式 provider 响应

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

public class ProviderStreamExample {

    public Flux<OpenAiStreamCompletionResponse> execute(OpenAiStreamClient streamClient) {
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，你是什么模型？"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.siliconflow.cn")
                        .apiKey("your-api-key")
                        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                        .build())
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.7)
                        .maxTokens(256)
                        .build())
                .build();

        return streamClient.streamCompletion(request);
    }
}
```

## 7. 说明

当前 Quick Start 对应的是项目第一版基础骨架。

当前已支持：

- 普通非流式对话调用
- 流式对话调用
- OpenAI-compatible provider 扩展响应字段保留

当前尚未覆盖：

- tools 完整闭环
- 多模态
- agent 编排层
