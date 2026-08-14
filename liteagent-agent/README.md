# liteagent-agent

`liteagent-agent` 是框架的通用编排层。
它不关心具体 provider 协议，只负责把一次调用拆成步骤并顺序调度。

## 职责

- 定义 chat（同步）和 stream（流式）两条编排路径
- 定义步骤接口与步骤 key
- 定义单次执行上下文
- 定义步骤 hook
- 定义执行终止原因

## 包结构

```text
io.github.halcyonsong.liteagent.agent
├─ chat                    # 同步编排路径
│  ├─ context
│  ├─ executor
│  ├─ hook
│  └─ step
├─ stream                  # 流式编排路径
│  ├─ context
│  ├─ executor
│  ├─ hook
│  ├─ state
│  └─ step
└─ state                   # 通用状态（终止原因等）
```

## chat 编排主线（同步）

```mermaid
flowchart TD
    A1[Invocation] --> A2[ChatAgent]
    A2 --> A3[ChatAgentExecutor]
    A3 --> A4[按 key 取出当前步骤]
    A4 --> A5[beforeStep hooks]
    A5 --> A6[ChatStep.invoke]
    A6 --> A7[afterStep hooks]
    A7 --> A8[写回 ChatAgentContext]
    A8 --> A9{next == END?}
    A9 -- no --> A4
    A9 -- yes --> A10[返回 Result / ChatAgentContext]
```

### chat 步骤 key

```text
BEGIN → MAP_REQUEST → ENHANCE_REQUEST → SEND_REQUEST → MAP_RESPONSE → ENHANCE_RESPONSE → ANALYZE_RESPONSE → BUILD_RESULT → END
```

## stream 编排主线（流式）

```mermaid
flowchart TD
    B1[Invocation] --> B2[StreamAgent]
    B2 --> B3[StreamAgentExecutor]
    B3 --> B4[同步准备阶段]
    B4 --> B5[BEGIN]
    B5 --> B6{首轮且消息为空?}
    B6 -- 是 --> B7[INIT_WORKING_MESSAGES]
    B6 -- 否 --> B8[MAP_REQUEST]
    B7 --> B8
    B8 --> B9[ENHANCE_REQUEST]
    B9 --> B10[SEND_REQUEST]
    B10 --> B11[流式管道阶段]
    B11 --> B12[ENHANCE_CHUNK]
    B12 --> B13[ACCUMULATE_CHUNK]
    B13 --> B14[ANALYZE_CHUNK]
    B14 --> B15[STREAM_END]
    B15 --> B16[expand 轮次调度]
    B16 --> B17{roundComplete?}
    B17 -- no --> B18[Flux.empty]
    B17 -- yes --> B19[DECIDE_NEXT_ACTION]
    B19 --> B20{决策结果}
    B20 -- BUILD_RESULT --> B21[BUILD_RESULT → END]
    B20 -- EXECUTE_TOOL --> B22[检查 maxIterations → EXECUTE_TOOL → 下一轮]
    B20 -- END --> B23[END]
```

### stream 步骤 key

```text
同步阶段: BEGIN → INIT_WORKING_MESSAGES → MAP_REQUEST → ENHANCE_REQUEST → SEND_REQUEST
流式阶段: SEND_REQUEST → ENHANCE_CHUNK → ACCUMULATE_CHUNK → ANALYZE_CHUNK → STREAM_END
调度阶段: DECIDE_NEXT_ACTION → BUILD_RESULT / EXECUTE_TOOL / END
```

## 已实现内容

### chat 路径

- `ChatAgent` — 门面入口
- `ChatAgentContext` — 请求级上下文
- `ChatAgentExecutor` — key-based 步骤推进执行器
- `ChatStep` / `ChatStepKey` — 步骤接口与 key 枚举
- `StepHook` — 步骤钩子（before / after / error）
- `AgentTerminationReason` — 终止原因枚举

### stream 路径

- `StreamAgent<T>` — 门面入口（泛型化）
- `StreamAgentContext<T>` — 请求级上下文（泛型化，含 workingMessages、rounds）
- `StreamAgentExecutor<T>` — 三阶段执行器（同步准备 → Flux 管道 → expand 轮次调度）
- `StreamStep<T>` / `StreamSyncStep` — 流式步骤接口 / 同步步骤接口
- `StreamApplyResult<T>` — 流式步骤返回值（output + nextKey）
- `StreamStepKey` — 步骤 key 枚举（13 个 key）
- `StreamRoundState` — 单轮状态（roundIndex、roundComplete、accumulator、finalResponse）
- `StreamStepHook` — 步骤钩子（before / after / error）

## ChatAgentContext 设计

`ChatAgentContext` 的生命周期只存在于一次执行内部，主要保存：

- `executionId`：本次执行唯一标识
- `invocation`：本次统一输入
- `attributes`：跨步骤共享扩展数据
- `result`：最终结果
- `iteration` / `maxIterations`：工具回环控制
- `terminationReason`：终止原因

## StreamAgentContext 设计

`StreamAgentContext<T>` 是流式路径的请求级上下文，额外包含：

- `workingMessages`：跨轮次维护的消息列表
- `rounds`：每轮的 `StreamRoundState` 列表
- `currentRound()`：获取当前轮次状态
- `output`：流式输出 Flux
- `iteration` / `maxIterations`：多轮工具调用控制

## 设计边界

当前模块不包含：

- provider 请求结构
- HTTP 调用实现
- 工具执行实现
- provider 响应映射

这些内容由上层 provider 模块或 provider-agent 模块负责。
