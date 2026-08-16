package io.github.halcyonsong.liteagent.core.model.response.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamDeltaTest {

    @Test
    void shouldCreateStreamDeltaSuccessfully() {
        StreamDelta delta = new StreamDelta("assistant", "你好", "思考内容", List.of());

        assertEquals("assistant", delta.getRole());
        assertEquals("你好", delta.getContent());
        assertEquals("思考内容", delta.getReasoningContent());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        StreamDelta delta = new StreamDelta("assistant", "hello", "thinking", List.of());

        String json = delta.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals("assistant", root.get("role").asText());
        assertEquals("hello", root.get("content").asText());
        assertEquals("thinking", root.get("reasoningContent").asText());
    }
}