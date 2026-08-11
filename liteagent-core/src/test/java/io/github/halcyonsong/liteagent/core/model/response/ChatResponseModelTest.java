package io.github.halcyonsong.liteagent.core.model.response;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatResponseModelTest {

    @Test
    void chat_response_should_keep_messages() {
        Message message = new AssistantMessage("answer");

        ChatResponse response = new ChatResponse(List.of(message));

        assertEquals(1, response.getMessages().size());
        assertEquals("answer", response.getMessages().get(0).getContent());
    }

    @Test
    void chat_response_should_fail_when_messages_null() {
        assertThrows(NullPointerException.class, () -> new ChatResponse(null));
    }

    @Test
    void chat_response_messages_should_be_unmodifiable() {
        ChatResponse response = new ChatResponse(List.of(new AssistantMessage("answer")));

        assertThrows(UnsupportedOperationException.class, () ->
                response.getMessages().add(new AssistantMessage("new")));
    }

    @Test
    void chat_choice_should_keep_fields() {
        ChatResponse response = new ChatResponse(List.of(new AssistantMessage("answer")));

        ChatChoice choice = new ChatChoice(0, response, FinishReason.STOP);

        assertEquals(0, choice.getIndex());
        assertSame(response, choice.getChatResponse());
        assertEquals(FinishReason.STOP, choice.getFinishReason());
    }

    @Test
    void chat_choice_should_fail_when_chat_response_null() {
        assertThrows(NullPointerException.class, () ->
                new ChatChoice(0, null, FinishReason.STOP));
    }

    @Test
    void usage_should_keep_fields() {
        Usage usage = new Usage(10, 20, 30);

        assertEquals(10, usage.getPromptTokens());
        assertEquals(20, usage.getCompletionTokens());
        assertEquals(30, usage.getTotalTokens());
    }

    @Test
    void chat_result_should_keep_fields() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion", 123L, "test-model");
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("answer"))),
                FinishReason.STOP
        );
        Usage usage = new Usage(10, 20, 30);

        ChatResult result = new ChatResult(baseResponse, List.of(choice), usage);

        assertSame(baseResponse, result.getBaseResponse());
        assertEquals(1, result.getChoices().size());
        assertSame(choice, result.getChoices().get(0));
        assertSame(usage, result.getUsage());
    }

    @Test
    void chat_result_should_fail_when_base_response_null() {
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("answer"))),
                FinishReason.STOP
        );

        assertThrows(NullPointerException.class, () ->
                new ChatResult(null, List.of(choice), null));
    }

    @Test
    void chat_result_should_fail_when_choices_null() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion", 123L, "test-model");

        assertThrows(NullPointerException.class, () ->
                new ChatResult(baseResponse, null, null));
    }

    @Test
    void chat_result_choices_should_be_unmodifiable() {
        BaseResponse baseResponse = new TestBaseResponse("resp_1", "chat.completion", 123L, "test-model");
        ChatChoice choice = new ChatChoice(
                0,
                new ChatResponse(List.of(new AssistantMessage("answer"))),
                FinishReason.STOP
        );

        ChatResult result = new ChatResult(baseResponse, List.of(choice), null);

        assertThrows(UnsupportedOperationException.class, () ->
                result.getChoices().add(choice));
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