# liteagent-provider-openai

`liteagent-provider-openai` 是 OpenAI-compatible 协议实现模块。

当前已完成普通与流式两条基础对话调用链路，并对请求、响应、运行时配置做了分层处理。

## 职责

该模块负责：

- 将 core 统一请求模型映射为 OpenAI-compatible raw request
- 发送普通或流式 HTTP 请求并接收 raw response
- 将 raw response 映射为 provider 响应对象
- 将 provider 响应进一步转换为 core 统一响应对象

## 包结构

```text
io.github.halcyonsong.liteagent.provider.openai
├─ client
├─ request
│  ├─ config
│  ├─ mapper
│  ├─ quickrequest
│  └─ raw
├─ response
│  ├─ config
│  │  ├─ chat
│  │  ├─ stream
│  │  └─ tool
│  ├─ mapper
│  └─ raw
├─ runtime
│  ├─ config
│  └─ register
├─ support
└─ transport
```

## 当前已实现内容

### request

- `OpenAiBaseRequest`
- `OpenAiChatCompletionRequest`
- `OpenAiCompletionOptions`
- `OpenAiQuickChatRequest`
- `OpenAiChatCompletionRawRequest`
- `OpenAiChatRequestMapper`

### response

普通响应：

- `OpenAiBaseResponse`
- `OpenAiUsage`
- `OpenAiChatCompletionResponse`
- `OpenAiAssistantMessage`
- `OpenAiToolCall`
- `OpenAiFunctionCall`
- `OpenAiChatResponseMapper`

流式响应：

- `OpenAiStreamCompletionResponse`
- `OpenAiStreamResponseMapper`

原始响应：

- `OpenAiChatCompletionRawResponse`

### runtime

- `HttpRuntimeConfig`
- `HttpRuntimeKey`
- `WebClientFactory`
- `WebClientRegistry`

### client / transport

普通调用：

- `OpenAiChatClient`
- `OpenAiChatTransport`

流式调用：

- `OpenAiStreamClient`
- `OpenAiChatStreamTransport`

快捷创建：

- `OpenAiChatClientFactory`
- `OpenAiClients`

## 当前设计说明

### 1. request / raw request 分层

provider 请求包装对象不直接等于最终发送 JSON。

- wrapper request：面向框架调用者
- raw request：面向协议发送层

### 2. response / raw response 分层

远端响应先完整接收到 raw response，再映射为 provider 响应对象。

这样可以同时保留：

- 统一模型返回能力
- provider 特有字段读取能力

### 3. 普通与流式调用分离

普通与流式请求通过不同客户端和不同 transport 区分：

- `OpenAiChatClient`：普通调用
- `OpenAiStreamClient`：流式调用

这样可以让：

- 普通返回模型稳定
- 流式返回模型语义清晰
- request / response / transport 链路更易维护

### 4. provider 特有响应字段保留在 provider 层

例如当前已保留：

- `reasoning_content`
- `tool_calls`
- provider 扩展 usage 字段

这些字段通过 provider 响应对象暴露，而不会直接进入 core 通用模型。

### 5. WebClient 运行时配置独立管理

基础运行时参数通过 runtime 模块控制，包括：

- 最大内存缓冲大小
- 连接超时
- 普通请求响应超时
- 流式请求响应超时（可为 `null`，表示不设置流式总响应超时）

## 当前未完成内容

- tools 完整回调链路
- 更完整的错误码接入
- 更细粒度 provider 配置能力
- Spring 自动装配支持

## 使用说明

当前推荐的调用入口包括：

普通：

- `ChatInvocation -> ChatResult`
- `OpenAiChatCompletionRequest -> OpenAiChatCompletionResponse`
- `OpenAiQuickChatRequest -> OpenAiChatCompletionResponse`

流式：

- `ChatInvocation -> Flux<StreamChunk>`
- `OpenAiChatCompletionRequest -> Flux<OpenAiStreamCompletionResponse>`
- `OpenAiQuickChatRequest -> Flux<OpenAiStreamCompletionResponse>`

更具体的调用示例可见 `liteagent-examples` 模块。
