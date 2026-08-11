# liteagent-template

一个面向 AI Agent / LLM 调用场景的轻量级 Java 框架骨架。

当前版本的重点不是直接实现完整 Agent 编排，而是先建立一套清晰、可扩展、边界明确的模型调用基础层：

- core 提供统一抽象
- provider 模块负责具体供应商协议实现
- examples 提供配置化示例与真实接口验证入口

## 当前状态

当前已完成的第一版基础骨架包括：

- 统一消息模型
- 统一请求模型
- 统一普通响应模型
- 统一流式响应模型
- OpenAI-compatible 普通对话调用
- OpenAI-compatible 流式对话调用
- provider request / raw request 分层
- provider response / raw response 分层
- OpenAI-compatible 扩展字段保留：
  - reasoning_content
  - tool_calls
  - 扩展 usage 字段
- 基础 WebClient 运行时配置与复用注册
- examples 模块下的 Spring Boot 配置化测试示例
- core / provider 的基础单元测试

当前尚未完成或仍处于后续规划中的内容：

- tools 完整闭环
- 多模态内容
- agent 编排层
- 更完整的异常细分与错误码体系
- Spring 自动装配支持
- 更系统的发布说明与使用文档

## 模块结构

```text
liteagent-template
├─ liteagent-core
├─ liteagent-provider-openai
└─ liteagent-examples
```

### liteagent-core

统一抽象层，不承载供应商特有协议字段。

主要包括：

- message：消息抽象与消息类型
- model.request：统一请求模型
- model.response.chat：统一普通响应模型
- model.response.stream：统一流式响应模型
- exception：框架基础异常类型

### liteagent-provider-openai

OpenAI-compatible 协议实现层。

主要包括：

- request：provider 包装请求、raw request、请求映射
- response：provider 包装响应、raw response、响应映射
- client：普通 / 流式调用入口
- transport：普通 / 流式 HTTP 发送与接收
- runtime：WebClient 运行时配置、工厂与注册表

### liteagent-examples

示例与手工验证模块。

当前用途：

- 演示 unified chat 调用方式
- 演示 provider chat 调用方式
- 演示 unified stream 调用方式
- 演示 provider stream 调用方式
- 通过 `application.yaml` 统一加载测试配置
- 用于真实接口 smoke test

## 当前设计原则

### 1. core 与 provider 解耦

core 只保留稳定、通用的抽象；供应商特有字段留在 provider 层。

例如：

- `ChatRequest` 只保留消息集合
- `ChatOptions` 只保留通用控制项
- OpenAI 的 `reasoning_content`、`tool_calls`、扩展 usage 字段仅保留在 openai provider 层

### 2. raw 与 wrapper 分层

对实际协议请求和响应采用两层结构：

- raw：与远端 JSON 结构一一对应
- wrapper：面向框架开发者暴露的 provider 结构

这样可以同时满足：

- 协议接收完整
- 上层调用语义更清晰
- 统一模型与供应商模型并存

### 3. 普通与流式分离

普通请求与流式请求不通过统一参数切换，而是通过不同客户端和不同方法名区分。

当前划分为：

- `OpenAiChatClient`：普通对话调用
- `OpenAiStreamClient`：流式对话调用

这样可以让：

- 普通返回模型稳定
- 流式返回模型语义清晰
- request / response / transport 链路更容易维护

### 4. 运行时与请求参数分离

WebClient 的基础运行时配置独立管理，单次请求参数动态传入。

已拆分为：

- runtime config
- runtime key
- webclient factory
- webclient registry

当前普通请求与流式请求使用独立的运行时超时语义：

- `responseTimeoutMillis`：普通请求响应超时
- `streamResponseTimeoutMillis`：流式请求总响应超时；为 `null` 时表示不设置流式总响应超时

## 快速开始

### 1. 构建项目

在项目根目录执行：

```bash
mvn clean test
```

### 2. 配置 example 模块

在 `liteagent-examples` 中通过 `application.yaml` 或环境变量配置：

- baseUrl
- apiKey
- model
- runtime 参数

### 3. 运行 example 测试

可以运行 `liteagent-examples` 下的测试类，验证：

- unified chat 调用
- provider chat 调用
- unified stream 调用
- provider stream 调用

## 使用文档

- [Quick Start](./docs/quick-start.md)
- [OpenAI-compatible Chat](./docs/openai-compatible-chat.md)

## 后续建议迭代顺序

建议按以下顺序继续推进：

1. 继续收紧 README、注释与文档
2. 收紧异常与错误码体系
3. 继续补齐 client / transport / runtime 测试
4. 完善 tools 完整链路
5. 增加多模态支持
6. 最后再引入 agent 编排层

## 说明

当前仓库更准确的定位是：

> 一个正在演进中的 LLM provider 基础调用框架骨架

而不是完整终态的 Agent 框架。
