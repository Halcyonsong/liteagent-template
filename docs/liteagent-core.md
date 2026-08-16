# liteagent-core 使用指南

本文档提供 `liteagent-core` 所有公共 API 的调用示例。

## 引入依赖

```xml
<dependency>
    <groupId>io.github.halcyonsong</groupId>
    <artifactId>liteagent-core</artifactId>
    <version>0.8.0-SNAPSHOT</version>
</dependency>
```

## 1. 消息构造

使用 `Messages` 工厂快速构造各类消息：

```java
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;

// 系统消息
SystemMessage system = Messages.system("你是一位助手");

// 用户消息
UserMessage user = Messages.user("你好");

// 助手消息（普通）
AssistantMessage assistant = Messages.assistant("你好，有什么可以帮你的？");

// 助手响应消息（含 reasoning 和 toolCalls）
AssistantResponseMessage response = Messages.assistantResponse(
        "北京今天晴，28°C",
        "用户询问天气，需要调用工具",
        List.of()
);

// 工具结果消息
ToolMessage toolResult = Messages.tool("{\"temperature\": 28}", "call_001");
```

## 2. 构造聊天请求

```java
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

ChatRequest chatRequest = ChatRequest.builder()
        .addMessage(Messages.system("你是一位助手"))
        .addMessage(Messages.user("你好"))
        .build();
```

## 3. 工具定义

使用三个注解定义工具：

```java
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;

@ToolComponent
public class WeatherTools {

    @ToolMethod(
            name = "get_weather",
            description = "获取指定城市的当前天气信息"
    )
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京") String city,
            @ToolParam(description = "温度单位", required = false) String unit
    ) {
        return city + "：晴，28" + (unit != null ? unit : "°C");
    }

    @ToolMethod(name = "get_time", description = "获取当前时间")
    public String getTime() {
        return java.time.LocalDateTime.now().toString();
    }
}
```

注解说明：

| 注解 | 作用目标 | 关键属性 |
|---|---|---|
| `@ToolComponent` | 类 | 标记可被批量扫描注册的类 |
| `@ToolMethod` | 方法 | `name`（工具名）、`description`（描述，必须非空） |
| `@ToolParam` | 参数 | `name`（覆盖反射参数名）、`description`、`required`（默认 true） |

## 4. 工具注册

```java
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

// 空注册表
ToolRegistry empty = ToolRegistries.inMemory();

// 单个工具类
ToolRegistry registry1 = ToolRegistries.inMemory(new WeatherTools());

// 多个工具类（varargs）
ToolRegistry registry2 = ToolRegistries.inMemory(new WeatherTools(), new TimeTools());

// 从列表注册
ToolRegistry registry3 = ToolRegistries.inMemory(List.of(new WeatherTools(), new TimeTools()));
```

注册表操作：

```java
// 查询
boolean exists = registry.contains("get_weather");
ToolDefinition tool = registry.get("get_weather");
List<ToolDefinition> all = registry.getAll();

// 工具定义信息
String name = tool.getName();           // "get_weather"
String desc = tool.getDescription();    // "获取指定城市的当前天气信息"
Map<String, Object> params = tool.getParameters();  // JSON Schema
```

## 5. 工具执行

```java
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;

// 从模型返回的 ToolCall 构造执行请求
ToolCall toolCall = new ToolCall(
        0,
        "call_001",
        "function",
        new FunctionCall("get_weather", "{\"city\":\"北京\"}")
);
ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

// 执行
ReflectionToolExecutor executor = new ReflectionToolExecutor();
Object result = executor.execute(request, registry);
// result = "北京：晴，28°C"
```

## 6. ToolExecutionRequest 便捷访问

```java
ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

// 直接获取工具名和参数 JSON
String toolName = request.getToolName();        // "get_weather"
String arguments = request.getArgumentsJson();  // "{\"city\":\"北京\"}"

// 序列化为 JSON
String json = request.toCompactJson();
```

## 7. 自定义 ObjectMapper

`ReflectionToolExecutor` 支持传入自定义 ObjectMapper：

```java
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper customMapper = new ObjectMapper();
// ... 配置 customMapper ...

ReflectionToolExecutor executor = new ReflectionToolExecutor(customMapper);
```

## 8. JSON 序列化

响应模型支持 JSON 序列化：

```java
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;

AssistantResponseMessage msg = Messages.assistantResponse("结果", "推理", List.of());

// 格式化 JSON
String pretty = msg.toJson();

// 紧凑 JSON
String compact = msg.toCompactJson();
```

## 9. 异常体系

core 模块中，工具执行相关的异常通过 `ToolExecutionException` 抛出：

```java
import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.exception.ErrorCode;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;

ToolCall toolCall = new ToolCall(0, "call_1", "function",
        new FunctionCall("get_weather", "invalid-json"));
ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

ReflectionToolExecutor executor = new ReflectionToolExecutor();

try {
    executor.execute(request, ToolRegistries.inMemory(new WeatherTools()));
} catch (ToolExecutionException e) {
    System.err.println("工具执行失败: " + e.getMessage());
    System.err.println("错误码: " + e.getErrorCode());
}
```

`ToolExecutionException` 可能由以下场景触发：

| 场景 | 异常消息示例 |
|---|---|
| 工具未注册 | `Tool not found in registry: xxx` |
| 必填参数缺失 | `Missing required tool argument: city` |
| JSON 解析失败 | `Failed to parse tool arguments JSON (length: N, preview: ...)` |
| 参数类型转换失败 | `Failed to convert argument to Integer: param` |
| 工具方法内部异常 | `Failed to execute tool: xxx` |

异常层级：

```
RuntimeException
└── LiteAgentException        (框架基础异常，含 ErrorCode)
    ├── ModelException        (模型调用异常，provider 层抛出)
    └── ToolExecutionException (工具执行异常，core 层抛出)
```

`ModelException` 在 core 模块中不会被直接抛出，它由 provider 层的 transport / response mapper 抛出，详见 provider 模块文档。
