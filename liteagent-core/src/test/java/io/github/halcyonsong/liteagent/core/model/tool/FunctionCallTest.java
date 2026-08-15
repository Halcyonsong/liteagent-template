package io.github.halcyonsong.liteagent.core.model.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionCallTest {

    @Test
    void should_create_with_name_and_arguments() {
        FunctionCall function = new FunctionCall("get_weather", "{\"city\":\"北京\"}");

        assertEquals("get_weather", function.getName());
        assertEquals("{\"city\":\"北京\"}", function.getArguments());
    }

    @Test
    void should_allow_null_fields() {
        FunctionCall function = new FunctionCall(null, null);

        assertNull(function.getName());
        assertNull(function.getArguments());
    }

    @Test
    void should_allow_empty_arguments() {
        FunctionCall function = new FunctionCall("search", "");

        assertEquals("search", function.getName());
        assertEquals("", function.getArguments());
    }
}
