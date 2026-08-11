# liteagent-core

`liteagent-core` 是框架的统一抽象层。

该模块只保留跨供应商相对稳定的基础模型，不直接承载某个供应商的协议特有字段。

## 职责

当前主要职责包括：

- 定义统一消息接口与消息类型
- 定义统一请求模型
- 定义统一普通响应模型
- 定义统一流式响应模型
- 定义框架基础异常类型

## 包结构

```text
io.github.halcyonsong.liteagent.core
├─ exception
├─ message
└─ model
   ├─ request
   └─ response
      ├─ chat
      └─ stream
```

## 当前已实现内容

### message

统一消息抽象：

- `Message`
- `AbstractMessage`
- `UserMessage`
- `AssistantMessage`
- `SystemMessage`
- `ToolMessage`
- `Messages` 快速构造工具

### request

统一请求模型：

- `BaseRequest`
- `ChatRequest`
- `ChatOptions`
- `ChatInvocation`

### response.chat

统一普通响应模型：

- `BaseResponse`
- `ChatResponse`
- `ChatChoice`
- `ChatResult`
- `Usage`

### response.stream

统一流式响应模型：

- `StreamDelta`
- `StreamChoice`
- `StreamChunk`

### exception

当前异常骨架：

- `LiteAgentException`
- `ModelException`
- `ToolExecutionException`

## 边界说明

以下内容暂时不应放入 `core`：

- OpenAI-compatible 的 `reasoning_content`
- OpenAI-compatible 的 `tool_calls`
- provider 特有扩展 usage 字段
- 供应商特有扩展参数
- 具体 HTTP 实现
- agent 编排逻辑

这些内容应保留在 provider 层或后续 agent 层。

## 当前定位

当前 `core` 的目标不是“大而全”，而是：

- 提供稳定抽象
- 控制边界清晰
- 为多个 provider 提供统一基础模型

后续若新增其他模型供应商，应优先复用该模块中的统一抽象。
