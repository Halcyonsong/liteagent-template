# liteagent-provider-openai 使用指南

本文档提供 `liteagent-provider-openai` 所有公共 API 的调用示例。

本模块是协议适配层，不包含 agent 编排。如需多轮工具调用编排，请结合 `liteagent-provider-openai-agent` 模块使用。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-provider-openai</artifactId>
    <version>${revision}</version>
</dependency>
```

> `${revision}` 为 `liteagent-parent` POM 中定义的版本属性。若您的项目未继承 `liteagent-parent`，请替换为实际版本号。

## 1. 构造运行时配置

`HttpRuntimeConfig` 控制底层 WebClient 的网络行为：

```java
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;

class RuntimeConfigExample {
    void build() {
        HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder()
                .maxInMemorySize(16 * 1024 * 1024)    // 响应体最大缓冲，默认 16MB
                .connectTimeoutMillis(5000)            // 连接超时，默认 5000ms
                .responseTimeoutMillis(60000L)         // 同步响应超时，默认 60000ms
                .streamResponseTimeoutMillis(null)     // 流式响应超时，默认不限制
                .build();
    }
}
```

## 2. WebClient 复用

### 方式一：全局共享 registry（推荐）

```java
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.OpenAiRuntime;
import org.springframework.web.reactive.function.client.WebClient;

class SharedRegistryExample {
    void demo(HttpRuntimeConfig runtimeConfig) {
        // 通过全局共享 registry 获取或创建 WebClient
        WebClient chatClient = OpenAiRuntime.getOrCreateChatClient(runtimeConfig);
        WebClient streamClient = OpenAiRuntime.getOrCreateStreamClient(runtimeConfig);
    }
}
```

相同配置的多次调用不会重复创建 WebClient，chat 和 stream 使用独立的缓存键。

### 方式二：自定义 registry

```java
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.HttpRuntimeMode;
import org.springframework.web.reactive.function.client.WebClient;

class CustomRegistryExample {
    void demo(HttpRuntimeConfig runtimeConfig) {
        WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());

        WebClient chatClient = registry.getOrCreateChatClient(runtimeConfig);
        WebClient streamClient = registry.getOrCreateStreamClient(runtimeConfig);

        // 管理缓存
        registry.remove(runtimeConfig, HttpRuntimeMode.CHAT);
        registry.clear();
        int cacheSize = registry.size();
    }
}
```

### 方式三：直接传入 WebClient

```java
import org.springframework.web.reactive.function.client.WebClient;

class DirectWebClientExample {
    void build() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .build();
    }
}
```

## 3. 构造 Provider 请求

### 标准构造

```java
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

class ProviderRequestExample {
    void build() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey("sk-xxx")
                .model("Qwen/Qwen2.5-7B-Instruct")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一位助手"))
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiCompletionOptions options = OpenAiCompletionOptions.builder()
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .completionOptions(options)
                .build();
    }
}
```

### 快速构造

```java
import io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest.OpenAiQuickChatRequest;

class QuickRequestExample {
    void build() {
        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl("https://api.siliconflow.cn")
                .apiKey("sk-xxx")
                .model("Qwen/Qwen2.5-7B-Instruct")
                .systemMessage("你是一位助手")
                .userMessage("你好")
                .build();

        // 转换为标准请求
        OpenAiChatCompletionRequest request = quick.toChatCompletion();
    }
}
```

## 4. CompletionOptions 参数

```java
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;

import java.util.List;
import java.util.Map;

class CompletionOptionsExample {
    void demo() {
        // 基本参数
        OpenAiCompletionOptions options1 = OpenAiCompletionOptions.builder()
                .temperature(0.7)
                .maxTokens(2048)
                .topP(0.9)
                .n(1)
                .presencePenalty(0.0)
                .frequencyPenalty(0.0)
                .build();

        // 停止序列
        OpenAiCompletionOptions options2 = OpenAiCompletionOptions.builder()
                .stop(OpenAiCompletionOptions.Stop.of("###"))
                .build();

        OpenAiCompletionOptions options3 = OpenAiCompletionOptions.builder()
                .stop(OpenAiCompletionOptions.Stop.of(List.of("###", "END")))
                .build();

        // JSON 返回格式
        OpenAiCompletionOptions options4 = OpenAiCompletionOptions.builder()
                .responseFormat(Map.of("type", "json_object"))
                .build();
    }
}
```

## 5. Transport 直调

### 同步请求

```java
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import org.springframework.web.reactive.function.client.WebClient;

class ChatTransportExample {
    void demo(WebClient webClient, OpenAiChatCompletionRequest request, OpenAiBaseRequest baseRequest) {
        // 映射为 raw request
        OpenAiChatRequestMapper mapper = new OpenAiChatRequestMapper();
        OpenAiChatCompletionRawRequest rawRequest = mapper.map(request);

        // 解析端点
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(baseRequest.getBaseUrl());

        // 发送
        OpenAiChatTransport chatTransport = new OpenAiChatTransport(webClient);
        OpenAiChatCompletionRawResponse rawResponse = chatTransport.send(endpoint, baseRequest.getApiKey(), rawRequest);
    }
}
```

### 流式请求

```java
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiStreamTransport;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

class StreamTransportExample {
    void demo(WebClient webClient, String endpoint, OpenAiBaseRequest baseRequest,
              OpenAiChatCompletionRawRequest rawRequest) {
        OpenAiStreamTransport streamTransport = new OpenAiStreamTransport(webClient);
        Flux<OpenAiChatCompletionRawResponse> stream =
                streamTransport.send(endpoint, baseRequest.getApiKey(), rawRequest);

        stream.doOnNext(chunk -> {
                    // 处理每个 SSE chunk
                    System.out.println(chunk);
                })
                .blockLast();
    }
}
```

## 6. 响应映射

### 同步响应

```java
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;

import java.util.List;

class ChatResponseMapperExample {
    void demo(OpenAiChatCompletionRawResponse rawResponse) {
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();
        AssistantResponseMessage message = responseMapper.map(rawResponse);

        // 读取结果
        String content = message.getContent();
        String reasoning = message.getReasoningContent();
        List<ToolCall> toolCalls = message.getToolCalls();
    }
}
```

### 流式响应

```java
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import reactor.core.publisher.Flux;

class StreamResponseMapperExample {
    void demo(Flux<OpenAiChatCompletionRawResponse> stream) {
        OpenAiStreamResponseMapper streamMapper = new OpenAiStreamResponseMapper();

        Flux<OpenAiStreamCompletionResponse> mappedStream = stream.map(chunk -> streamMapper.map(chunk));

        mappedStream.doOnNext(response -> {
                    String delta = response.getContent();
                    if (delta != null) {
                        System.out.print(delta);
                    }
                })
                .blockLast();
    }
}
```

## 7. Advisor 用法

### 注入工具定义

```java
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

class ToolsAdvisorExample {
    void demo(OpenAiBaseRequest baseRequest) {
        // 创建工具注册表
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        // 创建 advisor
        OpenAiRegistryToolsAdvisor toolsAdvisor = new OpenAiRegistryToolsAdvisor(registry);

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        // 在请求中注册 advisor
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .requestAdvisor(toolsAdvisor)
                .build();
    }
}
```

### 注入 tool_choice

```java
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiToolChoiceAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

class ToolChoiceExample {
    void demo(OpenAiBaseRequest baseRequest) {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());
        OpenAiRegistryToolsAdvisor toolsAdvisor = new OpenAiRegistryToolsAdvisor(registry);

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        // auto：模型自动决定是否调用工具
        OpenAiToolChoiceAdvisor autoAdvisor = new OpenAiToolChoiceAdvisor(OpenAiToolChoice.auto());

        // none：禁止调用工具
        OpenAiToolChoiceAdvisor noneAdvisor = new OpenAiToolChoiceAdvisor(OpenAiToolChoice.none());

        // required：必须调用工具
        OpenAiToolChoiceAdvisor requiredAdvisor = new OpenAiToolChoiceAdvisor(OpenAiToolChoice.required());

        // 指定函数：必须调用指定函数
        OpenAiToolChoiceAdvisor functionAdvisor = new OpenAiToolChoiceAdvisor(
                OpenAiToolChoice.function("get_weather")
        );

        // 注册到请求
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .requestAdvisor(toolsAdvisor)
                .requestAdvisor(functionAdvisor)
                .build();
    }
}
```

### 应用 Advisor

```java
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

class ApplyAdvisorExample {
    void demo(OpenAiChatCompletionRequest request,
              OpenAiChatCompletionRawRequest rawRequest,
              OpenAiChatCompletionRawResponse rawResponse,
              OpenAiChatCompletionResponse chatResponse,
              OpenAiStreamCompletionResponse streamResponse) {
        OpenAiAdvisorsExecutor advisorsExecutor = new OpenAiAdvisorsExecutor();

        // 在 mapper 产出 rawRequest 后、transport 发送前应用
        advisorsExecutor.applyRequestAdvisors(request, rawRequest);

        // 在 transport 返回 rawResponse 后、response mapper 处理前应用
        advisorsExecutor.applyChatResponseAdvisors(request, rawResponse, chatResponse);

        // 流式响应
        advisorsExecutor.applyStreamResponseAdvisors(request, rawResponse, streamResponse);
    }
}
```

## 8. 端点解析

```java
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;

class EndpointResolverExample {
    void demo() {
        // 自动补全 /v1/chat/completions 后缀
        String endpoint1 = OpenAiEndpointResolver.resolveChatCompletionsEndpoint("https://api.openai.com");
        // → "https://api.openai.com/v1/chat/completions"

        String endpoint2 = OpenAiEndpointResolver.resolveChatCompletionsEndpoint("https://api.openai.com/v1");
        // → "https://api.openai.com/v1/chat/completions"

        String endpoint3 = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.openai.com/v1/chat/completions");
        // → "https://api.openai.com/v1/chat/completions"（已完整，不重复补全）
    }
}
```
