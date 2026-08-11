package io.github.halcyonsong.liteagent.provider.openai.request.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiCompletionOptionsTest {

    @Test
    void build_should_keep_all_fields() {
        OpenAiCompletionOptions options = OpenAiCompletionOptions.builder()
                .topP(0.9)
                .n(2)
                .stop(OpenAiCompletionOptions.Stop.of("END"))
                .presencePenalty(1.2)
                .frequencyPenalty(0.8)
                .responseFormat(Map.of("type", "json_object"))
                .build();

        assertEquals(0.9, options.getTopP());
        assertEquals(2, options.getN());
        assertNotNull(options.getStop());
        assertEquals(1.2, options.getPresencePenalty());
        assertEquals(0.8, options.getFrequencyPenalty());
        assertEquals("json_object", options.getResponseFormat().get("type"));
    }

    @Test
    void stop_should_support_single_value() {
        OpenAiCompletionOptions.Stop stop = OpenAiCompletionOptions.Stop.of("END");

        assertTrue(stop.isSingle());
        assertFalse(stop.isMultiple());
        assertEquals("END", stop.getSingle());
        assertNull(stop.getMultiple());
        assertEquals("END", stop.toRawValue());
    }

    @Test
    void stop_should_support_multiple_values() {
        OpenAiCompletionOptions.Stop stop = OpenAiCompletionOptions.Stop.of(List.of("END", "STOP"));

        assertFalse(stop.isSingle());
        assertTrue(stop.isMultiple());
        assertNull(stop.getSingle());
        assertEquals(List.of("END", "STOP"), stop.getMultiple());
        assertEquals(List.of("END", "STOP"), stop.toRawValue());
    }

    @Test
    void stop_should_fail_when_single_value_null() {
        assertThrows(NullPointerException.class, () -> OpenAiCompletionOptions.Stop.of((String) null));
    }

    @Test
    void stop_should_fail_when_multiple_values_null() {
        assertThrows(NullPointerException.class, () -> OpenAiCompletionOptions.Stop.of((List<String>) null));
    }

    @Test
    void stop_should_fail_when_multiple_values_empty() {
        assertThrows(IllegalArgumentException.class, () -> OpenAiCompletionOptions.Stop.of(List.of()));
    }
}