# liteagent-examples

`liteagent-examples` 是调用方式展示模块，通过 Spring Boot 测试 + `@Configuration` Bean 注入的形式演示框架完整调用链路。

## 设计理念

所有示例统一通过 Spring DI 注入框架组件，而非手动调用静态工厂：

- `OpenAiConfig` 作为 `@Configuration` 类，通过 `@Bean` 方法创建 Agent、ToolRegistry、MemoryWindowStore 等组件
- `OpenAiExampleSupport` 作为测试基类，`@Autowired` 注入所有 Bean
- `application.yaml` 外部化配置，`application-local.yaml` 覆盖真实值

## Bean 装配结构

```
OpenAiConfig (@Configuration)
├── chatRuntimeConfig       — HttpRuntimeConfig（同步）
├── streamRuntimeConfig     — HttpRuntimeConfig（流式）
├── baseRequest             — OpenAiBaseRequest（baseUrl/apiKey/model）
├── weatherTools            — @ToolComponent 工具实例
├── toolRegistry            — ToolRegistry（注册 WeatherTools）
├── memoryWindowStore       — MemoryWindowStore（内存记忆窗口）
├── chatAgent               — OpenAiChatAgent（基础，无 hook）
├── streamAgent             — OpenAiStreamAgent（基础，无 hook）
├── chatAgentWithMemory     — OpenAiChatAgent（带记忆窗口 hook）
└── streamAgentWithMemory   — OpenAiStreamAgent（带记忆窗口 hook）
```

## 示例清单

### 基础对话

| 测试类 | 演示内容 |
|--------|---------|
| `ChatAgentExampleTest` | ChatAgent 同步对话 |
| `StreamAgentExampleTest` | StreamAgent 流式对话 |
| `ProviderChatExampleTest` | ChatAgent + CompletionOptions |
| `ProviderStreamExampleTest` | StreamAgent + CompletionOptions |
| `ProviderReasoningExampleTest` | reasoning（思考链）输出 |

### 工具调用

| 测试类 | 演示内容 |
|--------|---------|
| `ChatAgentToolCallExampleTest` | ChatAgent 多轮工具调用 |
| `StreamAgentToolCallExampleTest` | StreamAgent 多轮工具调用 |
| `ProviderToolCallExampleTest` | tool_choice 强制调用指定工具 |

### 记忆窗口

| 测试类 | 演示内容 |
|--------|---------|
| `MemoryChatExampleTest` | ChatAgent + sessionId 连续两轮记忆 |
| `MemoryStreamExampleTest` | StreamAgent + sessionId 连续两轮记忆 |

### Hook 与快捷请求

| 测试类 | 演示内容 |
|--------|---------|
| `AgentHookExampleTest` | StepHook / StreamStepHook 前后回调追踪 |
| `QuickRequestChatExampleTest` | OpenAiQuickChatRequest 快捷构造 + Chat |
| `ProviderQuickRequestStreamExampleTest` | OpenAiQuickChatRequest 快捷构造 + Stream |

## 配置方式

`application.yaml` 保留模板字段（环境变量占位）：

```yaml
liteagent:
  openai:
    enabled: ${LITEAGENT_OPENAI_ENABLED:true}
    base-url: ${LITEAGENT_OPENAI_BASE_URL:https://api.siliconflow.cn/v1/chat/completions}
    api-key: ${LITEAGENT_OPENAI_API_KEY:your-api-key}
    model: ${LITEAGENT_OPENAI_MODEL:your-model}
    runtime:
      max-in-memory-size: ${LITEAGENT_OPENAI_MAX_IN_MEMORY_SIZE:16777216}
      connect-timeout-millis: ${LITEAGENT_OPENAI_CONNECT_TIMEOUT_MILLIS:5000}
      response-timeout-millis: ${LITEAGENT_OPENAI_RESPONSE_TIMEOUT_MILLIS:60000}
      stream-response-timeout-millis: ${LITEAGENT_OPENAI_STREAM_RESPONSE_TIMEOUT_MILLIS:300000}
    memory:
      chat-max-size: ${LITEAGENT_MEMORY_CHAT_MAX_SIZE:40}
      stream-max-size: ${LITEAGENT_MEMORY_STREAM_MAX_SIZE:100}
```

`application-local.yaml` 覆盖真实值，建议加入 `.gitignore`。

## 运行方式

```bash
# 运行全部示例
mvn test -pl liteagent-examples

# 运行单个示例
mvn test -pl liteagent-examples -Dtest=MemoryChatExampleTest
```

未配置有效 API key 时，测试通过 `Assumptions.assumeTrue` 自动跳过。
