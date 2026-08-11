# OpenAI-compatible Chat

本文档说明当前 `liteagent-provider-openai` 模块的几种典型调用方式。

## 1. 统一调用方式

这是当前最推荐的对外调用方式。

优点：

- 使用 core 统一模型
- 对调用方最稳定
- 便于后续切换其他 provider

示例：

```java
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
        .baseRequest(OpenAiBaseRequest.builder()
                .baseUrl("https://api.siliconflow.cn/v1/chat/completions")
                .apiKey("your-api-key")
                .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                .build())
        .chatRequest(chatRequest)
        .chatOptions(chatOptions)
        .build();

ChatResult result = client.chat(invocation);
```

## 2. 直接使用 provider 请求对象

如果你需要传入 OpenAI-compatible 扩展参数，可以直接使用 provider 请求包装对象。

```java
OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
        .topP(0.9)
        .n(1)
        .build();

OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
        .baseRequest(OpenAiBaseRequest.builder()
                .baseUrl("https://api.siliconflow.cn/v1/chat/completions")
                .apiKey("your-api-key")
                .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
                .build())
        .chatRequest(chatRequest)
        .chatOptions(chatOptions)
        .completionOptions(completionOptions)
        .build();

OpenAiChatCompletionResponse response = client.chat(request);
```

## 3. 使用 QuickRequest 快速调用

适合：

- 快速测试
- 最短接入
- 示例场景

```java
OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
        .baseUrl("https://api.siliconflow.cn/v1/chat/completions")
        .apiKey("your-api-key")
        .model("deepseek-ai/DeepSeek-R1-0528-Qwen3-8B")
        .systemMessage("You are a helpful assistant.")
        .userMessage("你好，请简单介绍一下你自己。")
        .build();

ChatResult result = client.chat(request);
```

## 4. 读取 provider 扩展响应字段

当前 OpenAI-compatible provider 已保留：

- `reasoning_content`
- `tool_calls`

如果你需要读取这些字段，应获取 `OpenAiChatCompletionResponse`，并从消息中判断 provider 专属类型。

```java
public void printProviderResponse(OpenAiChatCompletionResponse response) {
    for (ChatChoice choice : response.getChoices()) {
        choice.getChatResponse().getMessages().forEach(message -> {
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
        });
    }
}
```

## 5. 统一结果与 provider 结果的区别

### `ChatResult`

面向统一抽象，适合上层业务稳定依赖。

特点：

- 不暴露 provider 特有字段
- 更适合后续多 provider 统一
- 结构更稳定

### `OpenAiChatCompletionResponse`

面向 OpenAI-compatible provider 特性。

特点：

- 可保留 `reasoning_content`
- 可保留 `tool_calls`
- 更适合需要读取供应商扩展响应时使用

## 6. example 模块配置方式

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

## 7. 当前限制

当前本文档对应能力仅限：

- 普通非流式对话调用
- OpenAI-compatible 协议
- provider 扩展字段读取

暂未覆盖：

- 流式响应
- tools 完整闭环
- 多模态
- agent 编排

这些能力建议作为后续阶段继续扩展。
