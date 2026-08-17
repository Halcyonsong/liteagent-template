# liteagent-memory

`liteagent-memory` 是框架的会话记忆模块。
它通过记忆窗口（MemoryWindow）维护每个会话的历史消息，在编排开始时把历史拼入工作消息，在编排结束时把本轮消息折叠后写回窗口。

## 职责

- 定义记忆窗口接口与内存实现
- 定义记忆窗口存储接口与内存实现
- 提供 Chat / Stream 两侧的 StepHook，自动加载历史和写回本轮
- 提供消息折叠逻辑，保证窗口内是干净的 user → assistant 交替序列

## 包结构

```text
io.github.halcyonsong.liteagent.memory
├─ window
│  ├─ norm                    # 窗口与存储接口
│  │  ├─ MemoryWindow
│  │  └─ MemoryWindowStore
│  ├─ impl                    # 内存实现
│  │  ├─ InMemoryMemoryWindow
│  │  └─ InMemoryMemoryWindowStore
│  └─ MemoryWindows           # 工厂入口
└─ hook
   ├─ support
   │  └─ MemoryHookSupport    # 共享逻辑（加载/写回/折叠）
   ├─ chat
   │  └─ MemoryChatStepHook    # Chat 侧 StepHook
   └─ stream
      └─ MemoryStreamStepHook  # Stream 侧 StreamStepHook
```

## 工作流程

```text
请求开始 (afterStep: INIT_WORKING_MESSAGES)
  window = store.getOrCreate(sessionId)
  history = window.messages()              # 取出全部历史，窗口清空
  workingMessages = [history..., system, user]

编排执行（多轮工具调用照常进行）

请求结束 (beforeStep: END)
  window = store.getOrCreate(sessionId)    # 同一个窗口，当前为空
  folded = foldMessages(workingMessages)   # 折叠：system 跳过、tool 跳过、连续 assistant 合并
  window.appendAll(folded)                 # 追加完整快照（历史+本轮）
  while (size > maxSize) removeEarliest()  # 窗口裁剪
```

## 消息折叠规则

一次编排结束后，workingMessages 可能包含多轮工具调用产生的消息：

```text
[system, user, assistant(text1), tool(result), assistant(text2), tool(result), assistant(text3)]
```

折叠后写回窗口的只有：

```text
[user, assistant(text1\ntext2\ntext3)]
```

- system 消息跳过（每次会话自带）
- tool 消息和 tool_calls 丢弃
- 连续 assistant 的 content 按顺序拼接（换行分隔）
- user 消息保留

历史消息已经折叠过，线性扫描不会对它们产生重复合并。

## 与 agent 模块的关系

记忆模块通过 StepHook 接入编排：

- Chat 侧：`MemoryChatStepHook` 实现 `StepHook`，在 `afterStep(INIT_WORKING_MESSAGES)` 加载历史，在 `beforeStep(END)` 写回
- Stream 侧：`MemoryStreamStepHook` 实现 `StreamStepHook`，时机相同

hook 在 agent 构造时注入，属于 agent 实例级配置。

## 与 provider 的关系

记忆模块完全供应商无关。它只依赖：

- core 层的 `Message` / `AssistantMessage` / `UserMessage` / `SystemMessage` / `ToolMessage`
- core 层的 `Invocation.getSessionId()`（default 方法，委托给 `ChatRequest.getSessionId()`）
- agent 层的 `StepHook` / `StreamStepHook` 接口

不需要任何 provider 适配代码。

## 持久化

框架不自动调用持久化。`MemoryWindowStore` 提供两个可重写的 default 方法：

- `loadHistory(sessionId)`：从外部存储加载历史，内存场景返回空列表
- `persist(sessionId, messages)`：将窗口快照写回外部存储，内存场景空操作

开发者需要在合适的时机显式调用 `store.persist(...)`，通常在请求结束后。

## 编译

```bash
mvn -pl liteagent-memory -am compile
```
