# liteagent-examples

`liteagent-examples` 是示例与手工验证模块。

该模块不属于框架核心打包内容，允许适度使用 Spring Boot 配置能力，方便本地调试和 smoke test。

## 职责

当前主要用于：

- 演示框架调用方式
- 验证 OpenAI-compatible 普通对话链路
- 通过配置文件统一管理测试参数
- 作为后续功能联调入口

## 当前用法

当前示例主要通过 Spring Boot 测试启动，并从配置文件中读取：

- `baseUrl`
- `apiKey`
- `model`
- runtime 配置

## 建议配置方式

建议在测试配置中提供以下内容：

```yaml
liteagent:
  examples:
    openai:
      enabled: true
      base-url: ${LITEAGENT_OPENAI_BASE_URL:}
      api-key: ${LITEAGENT_OPENAI_API_KEY:}
      model: ${LITEAGENT_OPENAI_MODEL:}
      runtime:
        max-in-memory-size: 16777216
        connect-timeout-millis: 5000
        response-timeout-millis: 60000
```

## 说明

该模块的定位不是正式测试主战场。

更推荐的职责划分是：

- `liteagent-core`：单元测试
- `liteagent-provider-openai`：单元测试 + 集成测试
- `liteagent-examples`：示例、手工验证、真实接口 smoke test

## 是否保留

建议保留。

原因：

- 后续新增流式调用时可以继续在这里联调
- 新增工具调用时也可以直接补充示例
- 对后续使用者来说，这里相当于 quick start 工程

随着项目演进，该模块可以逐步补充为更完整的示例集。
