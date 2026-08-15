package io.github.halcyonsong.liteagent.core.model.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolCallTest {

    @Test
    void should_create_with_all_fields() {
        FunctionCall function = new FunctionCall("get_weather", "{\"city\":\"北京\"}");
        ToolCall toolCall = new ToolCall(0, "call_1", "function", function);

        assertEquals(0, toolCall.getIndex());
        assertEquals("call_1", toolCall.getId());
        assertEquals("function", toolCall.getType());
        assertSame(function, toolCall.getFunction());
    }

    @Test
    void should_allow_null_fields() {
        ToolCall toolCall = new ToolCall(null, null, null, null);

        assertNull(toolCall.getIndex());
        assertNull(toolCall.getId());
        assertNull(toolCall.getType());
        assertNull(toolCall.getFunction());
    }

    @Test
    void should_allow_null_function_for_streaming_delta() {
        ToolCall toolCall = new ToolCall(0, null, null, null);

        assertNull(toolCall.getFunction());
        assertEquals(0, toolCall.getIndex());
    }
}
