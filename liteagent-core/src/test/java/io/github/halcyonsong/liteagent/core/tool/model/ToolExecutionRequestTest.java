package io.github.halcyonsong.liteagent.core.tool.model;

import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolExecutionRequestTest {

    @Test
    void from_should_create_request_from_tool_call() {
        FunctionCall function = new FunctionCall("get_weather", "{\"city\":\"北京\"}");
        ToolCall toolCall = new ToolCall(0, "call_1", "function", function);

        ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

        assertEquals(0, request.getIndex());
        assertEquals("call_1", request.getId());
        assertEquals("function", request.getType());
        assertSame(function, request.getFunction());
    }

    @Test
    void from_should_provide_convenience_getters() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall("search", "{\"q\":\"test\"}"));

        ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

        assertEquals("search", request.getToolName());
        assertEquals("{\"q\":\"test\"}", request.getArgumentsJson());
    }

    @Test
    void from_should_reject_null_tool_call() {
        assertThrows(NullPointerException.class, () -> ToolExecutionRequest.from(null));
    }

    @Test
    void from_should_reject_null_function() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function", null);

        ToolExecutionException ex = assertThrows(
                ToolExecutionException.class,
                () -> ToolExecutionRequest.from(toolCall)
        );
        assertTrue(ex.getMessage().contains("function must not be null"));
    }

    @Test
    void from_should_reject_blank_function_name() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall("", "{}"));

        ToolExecutionException ex = assertThrows(
                ToolExecutionException.class,
                () -> ToolExecutionRequest.from(toolCall)
        );
        assertTrue(ex.getMessage().contains("function name must not be blank"));
    }

    @Test
    void from_should_reject_null_function_name() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall(null, "{}"));

        assertThrows(ToolExecutionException.class, () -> ToolExecutionRequest.from(toolCall));
    }

    @Test
    void constructor_should_reject_null_function() {
        assertThrows(NullPointerException.class,
                () -> new ToolExecutionRequest(0, "call_1", "function", null));
    }

    @Test
    void to_json_should_produce_valid_json() {
        ToolCall toolCall = new ToolCall(0, "call_1", "function",
                new FunctionCall("get_weather", "{\"city\":\"北京\"}"));
        ToolExecutionRequest request = ToolExecutionRequest.from(toolCall);

        String json = request.toCompactJson();
        assertNotNull(json);
        assertTrue(json.contains("get_weather"));
        assertTrue(json.contains("call_1"));
    }
}
