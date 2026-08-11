# Quick Start

本文档给出 `liteagent-template` 当前第一版最短接入方式。

当前 Quick Start 面向：

- 普通非流式对话调用
- OpenAI-compatible 协议供应商
- Java 17

## 1. 引入依赖

根据当前模块设计，通常至少需要：

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

```java
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;

HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder()
        .maxInMemorySize(16 * 1024 * 1024)
        .connectTimeoutMillis(5000)
        .responseTimeoutMillis(60000L)
        .build();

WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());
OpenAiChatClientFactory factory = new OpenAiChatClientFactory(registry);
OpenAiChatClient client = factory.create(runtimeConfig);
```

## 3. 构造统一调用请求

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;

OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
        .baseUrl("https://api.siliconflow.cn/v1/chat/completions")
        .apiKey("your-api-key")
        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
        .build();

ChatRequest chatRequest = ChatRequest.builder()
        .addMessage(Messages.system("You are a helpful assistant."))
        .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
        .build();

ChatOptions chatOptions = ChatOptions.builder()
        .stream(false)
        .temperature(0.7)
        .maxTokens(256)
        .build();

ChatInvocation invocation = ChatInvocation.builder()
        .baseRequest(baseRequest)
        .chatRequest(chatRequest)
        .chatOptions(chatOptions)
        .build();
```

## 4. 发起调用并读取统一结果

```java
import io.github.halcyonsong.liteagent.core.model.response.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;

public void printChatResult(OpenAiChatClient client, ChatInvocation invocation) {
    ChatResult result = client.chat(invocation);

    System.out.println("response id = " + result.getBaseResponse().getId());
    System.out.println("model = " + result.getBaseResponse().getModel());

    for (ChatChoice choice : result.getChoices()) {
        System.out.println("choice index = " + choice.getIndex());
        System.out.println("finish reason = " + choice.getFinishReason());
        choice.getChatResponse().getMessages().forEach(message ->
                System.out.println("assistant content = " + message.getContent())
        );
    }
}
```

## 5. 什么时候使用 provider 响应而不是统一结果

如果你只关心通用对话内容，优先使用：

```java
ChatResult result = client.chat(invocation);
```

如果你需要读取 OpenAI-compatible 协议中的扩展字段，例如：

- `reasoning_content`
- `tool_calls`

则应使用 provider 响应对象：

```java
OpenAiChatCompletionResponse response = client.chatCompletion(invocation);
```

具体示例见：

- [OpenAI-compatible Chat](./openai-compatible-chat.md)

## 6. 说明

当前 Quick Start 对应的是项目第一版基础骨架。

当前尚未覆盖：

- 流式响应
- 工具调用完整链路
- agent 编排层

这些功能建议在基础普通对话链路稳定后再继续扩展。
