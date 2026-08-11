package io.github.halcyonsong.liteagent.core.model.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatOptionsTest {

    @Test
    void build_should_keep_fields() {
        ChatOptions options = ChatOptions.builder()
                .stream(false)
                .temperature(0.7)
                .maxTokens(256)
                .build();

        assertEquals(false, options.getStream());
        assertEquals(0.7, options.getTemperature());
        assertEquals(256, options.getMaxTokens());
    }

    @Test
    void build_should_allow_all_fields_null() {
        ChatOptions options = ChatOptions.builder().build();

        assertNull(options.getStream());
        assertNull(options.getTemperature());
        assertNull(options.getMaxTokens());
    }
}