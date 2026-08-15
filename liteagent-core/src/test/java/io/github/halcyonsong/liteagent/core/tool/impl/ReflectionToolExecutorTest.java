package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionToolExecutorTest {

    private ReflectionToolExecutor executor;
    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = ToolRegistries.inMemory(new TestTools());
        executor = new ReflectionToolExecutor();
    }

    @Test
    void execute_should_call_method_with_string_param() {
        ToolExecutionRequest request = createRequest("echo", "{\"text\":\"hello\"}");
        Object result = executor.execute(request, registry);
        assertEquals("hello", result);
    }

    @Test
    void execute_should_call_method_with_multiple_params() {
        ToolExecutionRequest request = createRequest("add", "{\"a\":3,\"b\":5}");
        Object result = executor.execute(request, registry);
        assertEquals(8, result);
    }

    @Test
    void execute_should_handle_optional_param_missing() {
        ToolExecutionRequest request = createRequest("greet", "{\"name\":\"World\"}");
        Object result = executor.execute(request, registry);
        assertEquals("Hello, World!", result);
    }

    @Test
    void execute_should_handle_optional_param_provided() {
        ToolExecutionRequest request = createRequest("greet", "{\"name\":\"World\",\"greeting\":\"Hi\"}");
        Object result = executor.execute(request, registry);
        assertEquals("Hi, World!", result);
    }

    @Test
    void execute_should_handle_boolean_param() {
        ToolExecutionRequest request = createRequest("toggle", "{\"flag\":true}");
        Object result = executor.execute(request, registry);
        assertEquals("ON", result);
    }

    @Test
    void execute_should_handle_string_to_integer_conversion() {
        ToolExecutionRequest request = createRequest("double_value", "{\"n\":\"42\"}");
        Object result = executor.execute(request, registry);
        assertEquals(84, result);
    }

    @Test
    void execute_should_throw_when_tool_not_found() {
        ToolExecutionRequest request = createRequest("nonexistent", "{}");
        ToolExecutionException ex = assertThrows(
                ToolExecutionException.class,
                () -> executor.execute(request, registry)
        );
        assertTrue(ex.getMessage().contains("Tool not found"));
    }

    @Test
    void execute_should_throw_when_missing_required_param() {
        ToolExecutionRequest request = createRequest("add", "{\"a\":1}");
        ToolExecutionException ex = assertThrows(
                ToolExecutionException.class,
                () -> executor.execute(request, registry)
        );
        assertTrue(ex.getMessage().contains("Missing required"));
    }

    @Test
    void execute_should_throw_when_invalid_json() {
        ToolExecutionRequest request = createRequest("echo", "not-json");
        assertThrows(ToolExecutionException.class, () -> executor.execute(request, registry));
    }

    @Test
    void execute_should_throw_when_null_request() {
        assertThrows(NullPointerException.class, () -> executor.execute(null, registry));
    }

    @Test
    void execute_should_throw_when_null_registry() {
        ToolExecutionRequest request = createRequest("echo", "{}");
        assertThrows(NullPointerException.class, () -> executor.execute(request, null));
    }

    @Test
    void execute_should_handle_empty_arguments() {
        ToolExecutionRequest request = createRequest("no_params", "{}");
        Object result = executor.execute(request, registry);
        assertEquals("ok", result);
    }

    @Test
    void execute_should_handle_null_arguments() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall("no_params", null));
        ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);
        Object result = executor.execute(request, registry);
        assertEquals("ok", result);
    }

    @Test
    void execute_should_wrap_method_exception() {
        ToolExecutionRequest request = createRequest("throw_error", "{\"msg\":\"boom\"}");
        ToolExecutionException ex = assertThrows(
                ToolExecutionException.class,
                () -> executor.execute(request, registry)
        );
        assertTrue(ex.getMessage().contains("Failed to execute"));
    }

    private static ToolExecutionRequest createRequest(String toolName, String arguments) {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall(toolName, arguments));
        return ToolExecutionRequest.from(toolCall);
    }

    @ToolComponent
    public static class TestTools {

        @ToolMethod(name = "echo", description = "回显文本")
        public String echo(@ToolParam(description = "文本") String text) {
            return text;
        }

        @ToolMethod(name = "add", description = "加法")
        public int add(
                @ToolParam(description = "a") int a,
                @ToolParam(description = "b") int b
        ) {
            return a + b;
        }

        @ToolMethod(name = "greet", description = "问候")
        public String greet(
                @ToolParam(description = "名字") String name,
                @ToolParam(description = "问候语", required = false) String greeting
        ) {
            return (greeting != null ? greeting : "Hello") + ", " + name + "!";
        }

        @ToolMethod(name = "toggle", description = "开关")
        public String toggle(@ToolParam(description = "开关") boolean flag) {
            return flag ? "ON" : "OFF";
        }

        @ToolMethod(name = "double_value", description = "翻倍")
        public int doubleValue(@ToolParam(description = "数值") int n) {
            return n * 2;
        }

        @ToolMethod(name = "no_params", description = "无参数")
        public String noParams() {
            return "ok";
        }

        @ToolMethod(name = "throw_error", description = "抛异常")
        public String throwError(@ToolParam(description = "消息") String msg) {
            throw new RuntimeException(msg);
        }
    }
}
