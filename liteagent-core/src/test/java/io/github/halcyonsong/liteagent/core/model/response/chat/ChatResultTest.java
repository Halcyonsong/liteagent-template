package io.github.halcyonsong.liteagent.core.model.response.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;
import io.github.halcyonsong.liteagent.core.model.response.Usage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatResultTest {

    @Test
    void shouldCreateChatResultSuccessfully() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion", 123L, "gpt-test");
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("最终答案"))),
                FinishReason.STOP
        );
        Usage usage = new Usage(10, 5, 15);

        ChatResult result = new ChatResult(baseResponse, List.of(choice), usage);

        assertEquals(baseResponse, result.getBaseResponse());
        assertEquals(1, result.getChoices().size());
        assertEquals(choice, result.getChoices().get(0));
        assertEquals(usage, result.getUsage());
    }

    @Test
    void shouldRejectNullBaseResponse() {
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("hello"))),
                FinishReason.STOP
        );

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ChatResult(null, List.of(choice), null)
        );

        assertTrue(exception.getMessage().contains("baseResponse must not be null"));
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion", 123L, "gpt-test");
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("hello"))),
                FinishReason.STOP
        );
        ChatResult result = new ChatResult(baseResponse, List.of(choice), new Usage(10, 5, 15));

        String json = result.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals("resp_1", root.get("baseResponse").get("id").asText());
        assertEquals("gpt-test", root.get("baseResponse").get("model").asText());
        assertEquals("hello",
                root.get("choices").get(0).get("chatResponse").get("messages").get(0).get("content").asText());
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