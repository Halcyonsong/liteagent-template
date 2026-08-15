# liteagent-core

`liteagent-core` 是框架的统一抽象层。
这里只保留跨供应商稳定的基础模型和规范，不放 OpenAI-compatible 这类协议私有字段。

## 职责

- 定义统一消息模型
- 定义统一请求抽象
- 定义统一结果抽象
- 定义统一流式结果模型
- 定义工具注册规范
- 定义框架基础异常

## 包结构

```text
io.github.halcyonsong.liteagent.core
├─ client
├─ exception
├─ message
├─ model
│  ├─ request
│  │  └─ norm
│  └─ response
│     ├─ chat
│     └─ stream
├─ support
└─ tool
   ├─ annotation
   ├─ impl
   └─ norm
```

## 已实现内容

### message

统一消息抽象：

- `Message`
- `AbstractMessage`
- `UserMessage`
- `AssistantMessage`
- `AssistantResponseMessage` — 含 `reasoningContent` 和 `toolCalls`
- `SystemMessage`
- `ToolMessage`
- `Messages`

### request.norm

统一请求抽象：

- `BaseRequest`
- `BaseOptions`
- `Invocation`
- `RequestAdvisor`

### request.impl

稳定请求模型：

- `ChatRequest`

### response

统一结果抽象：

- `Result`
- `BaseResponse`
- `Usage`
- `ChatChoice`
- `ChatResponse`
- `ChatResult`
- `StreamDelta`
- `StreamChoice`
- `StreamChunk`

### model.tool

工具模型：

- `ToolCall` — 工具调用（含 `index`、`id`、`type`、`function`）
- `FunctionCall` — 函数调用（含 `name`、`arguments`）

### tool

工具注册与执行规范：

- `@ToolComponent`
- `@ToolMethod`
- `@ToolParam`
- `ToolDefinition` / `ExecutableToolDefinition`
- `ToolRegistry`
- `ToolRegistrar`
- `ToolExecutor`
- `InMemoryToolRegistry`
- `ReflectionToolRegistrar`
- `ReflectiveToolDefinition`
- `ReflectionToolExecutor`
- `ToolRegistries`
- `ToolExecutionRequest`

### exception

基础异常：

- `LiteAgentException`
- `ModelException`
- `ToolExecutionException`

## 工具注册与执行链路

```mermaid
flowchart LR
    A[工具类] --> B[@ToolComponent]
    B --> C[@ToolMethod]
    C --> D[ReflectionToolRegistrar]
    D --> E[ReflectiveToolDefinition]
    E --> F[InMemoryToolRegistry]
    F --> G[provider 请求增强器]
    G --> H[raw request.tools]
    H --> I[agent ANALYZE_RESPONSE]
    I --> J[ReflectionToolExecutor]
    J --> K[ToolMessage]
```

## 设计边界

core 不直接包含这些内容：

- OpenAI-compatible `tool_calls` 协议字段
- provider 扩展 `usage`
- 具体 HTTP 实现
- agent 编排步骤链

这些内容留在 provider 或 provider-agent 层。
core 提供工具执行器（`ReflectionToolExecutor`）但不管编排调度。

## 使用原则

- 单个方法注册时，不要求类必须带 `@ToolComponent`
- 批量扫描注册时，只扫描带 `@ToolComponent` 的类
- 方法参数默认通过反射获取名称
- `@ToolParam.name()` 可覆盖反射结果
- `@ToolParam.required=false` 时，该参数不会进入 schema 的 required 列表
