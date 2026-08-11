package io.github.halcyonsong.liteagent.core.model.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatOptionsTest {

    @Test
    void shouldCreateEmptyChatOptions() {
        ChatOptions options = ChatOptions.builder().build();

        assertNull(options.getTemperature());
        assertNull(options.getMaxTokens());
    }

    @Test
    void shouldCreateChatOptionsWithValues() {
        ChatOptions options = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(512)
                .build();

        assertEquals(0.7, options.getTemperature());
        assertEquals(512, options.getMaxTokens());
    }
}