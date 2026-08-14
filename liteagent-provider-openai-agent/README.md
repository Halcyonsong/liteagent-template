# liteagent-provider-openai-agent

`liteagent-provider-openai-agent` 是 OpenAI provider 的编排接入层。
它把 `liteagent-provider-openai` 已有的 request mapper、advisor、transport、response mapper 装配成可执行步骤链。

## 职责

- 提供 OpenAI chatAgent 门面（同步）
- 提供 OpenAI streamAgent 门面（流式）
- 提供 chatAgent / streamAgent 执行器工厂
- 提供 OpenAI provider 的同步步骤链和流式步骤链

## 包结构

```text
io.github.halcyonsong.liteagent.provider.openai.agent
├─ chat                        # 同步编排实现
│  ├─ constant
│  ├─ factory
│  └─ step
│     ├─ request
│     └─ response
├─ stream                      # 流式编排实现
│  ├─ constant
│  ├─ factory
│  ├─ state
│  ├─ step
│  │  ├─ request
│  │  └─ response
│  └─ support
├─ OpenAiChatAgent             # 同步门面
└─ OpenAiStreamAgent           # 流式门面
```

## chat 编排主线（同步）

```mermaid
flowchart TD
    A1[Invocation] --> A2[OpenAiChatAgent.execute]
    A2 --> A3[ChatAgentExecutor]
    A3 --> A4[BEGIN]
    A4 --> A5[MAP_REQUEST]
    A5 --> A6[ENHANCE_REQUEST]
    A6 --> A7[SEND_REQUEST]
    A7 --> A8[MAP_RESPONSE]
    A8 --> A9[ENHANCE_RESPONSE]
    A9 --> A10[ANALYZE_RESPONSE]
    A10 --> A11[BUILD_RESULT]
    A11 --> A12[END]
```

## stream 编排主线（流式）

```mermaid
flowchart TD
    B1[Invocation] --> B2[OpenAiStreamAgent.execute]
    B2 --> B3[StreamAgentExecutor]
    B3 --> B4[同步准备]
    B4 --> B5[BEGIN]
    B5 --> B6[INIT_WORKING_MESSAGES]
    B6 --> B7[MAP_REQUEST]
    B7 --> B8[ENHANCE_REQUEST]
    B8 --> B9[SEND_REQUEST]
    B9 --> B10[流式管道]
    B10 --> B11[ENHANCE_CHUNK]
    B11 --> B12[ACCUMULATE_CHUNK]
    B12 --> B13[ANALYZE_CHUNK]
    B13 --> B14[STREAM_END]
    B14 --> B15[expand 调度]
    B15 --> B16[DECIDE_NEXT_ACTION]
    B16 --> B17[BUILD_RESULT]
    B17 --> B18[END]
```

## 已实现内容

### chat 路径（同步）

- `OpenAiChatAgent` — 门面
- `OpenAiChatAgents` — 工厂入口（支持 WebClient / HttpRuntimeConfig）
- `OpenAiChatAgentExecutorFactory` — 执行器装配
- `OpenAiChatBeginStep` — 初始化
- `OpenAiChatMapRequestStep` — 请求映射
- `OpenAiChatEnhanceRequestStep` — 请求增强（advisor + stream=false）
- `OpenAiChatSendRequestStep` — 发送 HTTP 请求
- `OpenAiChatMapResponseStep` — 响应映射
- `OpenAiChatEnhanceResponseStep` — 响应增强
- `OpenAiChatAnalyzeResponseStep` — 分析响应
- `OpenAiChatBuildResultStep` — 构建结果

### stream 路径（流式）

- `OpenAiStreamAgent` — 门面
- `OpenAiStreamAgents` — 工厂入口（支持 WebClient / HttpRuntimeConfig）
- `OpenAiStreamAgentExecutorFactory` — 执行器装配
- `OpenAiStreamBeginStep` — 初始化（判断是否需要初始化消息）
- `OpenAiStreamInitWorkingMessagesStep` — 复制 invocation 消息到 workingMessages
- `OpenAiStreamMapRequestStep` — 基于 workingMessages 构建请求
- `OpenAiStreamEnhanceRequestStep` — 请求增强（advisor + stream=true）
- `OpenAiStreamSendRequestStep` — 发起流式请求，创建源流
- `OpenAiStreamEnhanceChunkStep` — 对每个 chunk 应用 advisor
- `OpenAiStreamAccumulateChunkStep` — 累积 chunk 到 accumulator
- `OpenAiStreamAnalyzeChunkStep` — 标记轮次完成
- `OpenAiStreamDecideNextActionStep` — 决策下一步（当前恒返回 BUILD_RESULT）
- `OpenAiStreamExecuteToolStep` — 工具执行（当前返回 END）
- `OpenAiStreamBuildResultStep` — 构建结果
- `OpenAiStreamRoundAccumulator` — 单轮流式累积器
- `OpenAiStreamRoundSupport` — 累积器获取工具

## 当前范围

当前这个模块已经实现：

- 同步 chat 编排完整闭环
- 流式 stream 编排最小链路（单轮）
- 请求增强器（request advisor / response advisor）
- 流式 chunk 累积

还未实现：

- 工具自动执行闭环（DECIDE_NEXT_ACTION 恒返回 BUILD_RESULT）
- 多轮流式编排（EXECUTE_TOOL 为占位实现）
- 流式 chunk delta 合并（当前只收集不合并）
