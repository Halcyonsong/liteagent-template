package io.github.halcyonsong.liteagent.core.model.response.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatChoiceTest {

    @Test
    void shouldCreateChatChoiceSuccessfully() {
        ChatResponse response = new ChatResponse(List.of(new AssistantMessage("结果内容")));
        ChatChoice choice = new ChatChoice(0, response, FinishReason.STOP);

        assertEquals(0, choice.getIndex());
        assertEquals(response, choice.getChatResponse());
        assertEquals(FinishReason.STOP, choice.getFinishReason());
    }

    @Test
    void shouldRejectNullChatResponse() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ChatChoice(0, null, FinishReason.STOP)
        );

        assertTrue(exception.getMessage().contains("chatResponse must not be null"));
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        ChatChoice choice = new ChatChoice(
                1,
                new ChatResponse(List.of(new AssistantMessage("hello"))),
                FinishReason.LENGTH
        );

        String json = choice.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals(1, root.get("index").asInt());
        assertEquals("LENGTH", root.get("finishReason").asText());
        assertEquals("hello",
                root.get("chatResponse").get("messages").get(0).get("content").asText());
    }
}