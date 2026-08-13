# liteagent-examples

`liteagent-examples` 是示例和手工验证模块。
它不属于框架核心打包内容，但可以使用 Spring Boot 配置能力来做本地测试和 smoke test。

## 职责

- 演示普通 chat 调用
- 演示流式 chat 调用
- 演示 provider 扩展字段读取
- 演示 tools 注入
- 演示 tool_choice 注入
- 演示本地配置拆分

## 当前用法

示例通过 `application.yaml` 读取公共配置，再通过副配置文件覆盖本地密钥。

推荐形式：

```yaml
spring:
  config:
    import: optional:classpath:application-local.yaml
```

主配置只保留模板字段，副配置放真实值：

```yaml
liteagent:
  openai:
    enabled: true
    base-url: ${LITEAGENT_OPENAI_BASE_URL:}
    api-key: ${LITEAGENT_OPENAI_API_KEY:}
    model: ${LITEAGENT_OPENAI_MODEL:}
    runtime:
      max-in-memory-size: 16777216
      connect-timeout-millis: 5000
      response-timeout-millis: 60000
      stream-response-timeout-millis: 300000
```

## 推荐保留的示例类别

### unified

- `InvocationChatExampleTest`
- `QuickRequestChatExampleTest`
- `InvocationStreamExampleTest`

### provider

- `ProviderChatExampleTest`
- `ProviderReasoningExampleTest`
- `ProviderStreamExampleTest`
- `ProviderToolCallExampleTest`

### tool

- 工具注册验证
- tool_choice 验证
- 流程内工具增强验证

## 示例定位

这个模块更像一个“可运行文档”，不是最终业务代码。

适合做这些事：

- 验证接口是否还能正常调用
- 验证新特性是否按预期工作
- 作为仓库的调用模板

## 建议保留

建议保留这个模块。
原因很简单：

- 后续新增 provider 或工具链时，还是需要一个地方做真实 smoke test
- 对使用者来说，这里也是最直接的入门样例
