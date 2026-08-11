package io.github.halcyonsong.liteagent.core.model.response.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;
import io.github.halcyonsong.liteagent.core.model.response.Usage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamChunkTest {

    @Test
    void shouldCreateStreamChunkSuccessfully() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion.chunk", 123L, "gpt-test");
        StreamChoice choice = new StreamChoice(
                0,
                new StreamDelta("assistant", "你好", "推理"),
                FinishReason.UNKNOWN
        );
        Usage usage = new Usage(10, 5, 15);

        StreamChunk chunk = new StreamChunk(baseResponse, List.of(choice), usage);

        assertEquals(baseResponse, chunk.getBaseResponse());
        assertEquals(1, chunk.getChoices().size());
        assertEquals(choice, chunk.getChoices().get(0));
        assertEquals(usage, chunk.getUsage());
    }

    @Test
    void shouldRejectNullBaseResponse() {
        StreamChoice choice = new StreamChoice(
                0,
                new StreamDelta("assistant", "hello", null),
                FinishReason.UNKNOWN
        );

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new StreamChunk(null, List.of(choice), null)
        );

        assertTrue(exception.getMessage().contains("baseResponse must not be null"));
    }

    @Test
    void shouldRejectNullChoices() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion.chunk", 123L, "gpt-test");

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new StreamChunk(baseResponse, null, null)
        );

        assertTrue(exception.getMessage().contains("choices must not be null"));
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion.chunk", 123L, "gpt-test");
        StreamChoice choice = new StreamChoice(
                0,
                new StreamDelta("assistant", "hello", "thinking"),
                FinishReason.STOP
        );
        StreamChunk chunk = new StreamChunk(baseResponse, List.of(choice), new Usage(10, 5, 15));

        String json = chunk.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals("resp_1", root.get("baseResponse").get("id").asText());
        assertEquals("gpt-test", root.get("baseResponse").get("model").asText());
        assertEquals("hello", root.get("choices").get(0).get("delta").get("content").asText());
        assertEquals("thinking", root.get("choices").get(0).get("delta").get("reasoningContent").asText());
    }

    private static class TestBaseResponse implements BaseResponse {
        private final String id;
        private final String object;
        private final Long created;
        private final String model;

        private TestBaseResponse(String id, String object, Long created, String model) {
            this.id = id;
            this.object = object;
            this.created = created;
            this.model = model;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getObject() {
            return object;
        }

        @Override
        public Long getCreated() {
            return created;
        }

        @Override
        public String getModel() {
            return model;
        }
    }
}