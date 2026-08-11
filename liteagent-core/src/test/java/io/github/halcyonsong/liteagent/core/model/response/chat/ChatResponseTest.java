package io.github.halcyonsong.liteagent.core.model.response.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatResponseTest {

    @Test
    void shouldCreateChatResponseSuccessfully() {
        ChatResponse response = new ChatResponse(List.of(
                new UserMessage("用户消息"),
                new AssistantMessage("助手回复")
        ));

        assertEquals(2, response.getMessages().size());
        assertEquals("用户消息", response.getMessages().get(0).getContent());
        assertEquals("助手回复", response.getMessages().get(1).getContent());
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        ChatResponse response = new ChatResponse(List.of(
                new AssistantMessage("hello")
        ));

        String json = response.toJson();
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals("hello", root.get("messages").get(0).get("content").asText());
    }
}