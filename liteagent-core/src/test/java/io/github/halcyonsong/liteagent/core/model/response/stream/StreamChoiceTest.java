package io.github.halcyonsong.liteagent.core.model.response.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreamChoiceTest {

    @Test
    void shouldCreateStreamChoiceSuccessfully() {
        StreamDelta delta = new StreamDelta("assistant", "内容", "推理");
        StreamChoice choice = new StreamChoice(0, delta, FinishReason.STOP);

        assertEquals(0, choice.getIndex());
        assertEquals(delta, choice.getDelta());
        assertEquals(FinishReason.STOP, choice.getFinishReason());
    }

    @Test
    void shouldRejectNullDelta() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new StreamChoice(0, null, FinishReason.STOP)
        );

        assertTrue(exception.getMessage().contains("delta must not be null"));
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        StreamDelta delta = new StreamDelta("assistant", "hello", "thinking");
        StreamChoice choice = new StreamChoice(1, delta, FinishReason.LENGTH);

        String json = choice.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals(1, root.get("index").asInt());
        assertEquals("LENGTH", root.get("finishReason").asText());
        assertEquals("assistant", root.get("delta").get("role").asText());
        assertEquals("hello", root.get("delta").get("content").asText());
        assertEquals("thinking", root.get("delta").get("reasoningContent").asText());
    }
}