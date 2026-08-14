package io.github.halcyonsong.liteagent.core.model.request;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatRequestTest {

    @Test
    void shouldBuildChatRequestWithMessages() {
        ChatRequest request = ChatRequest.builder()
                .addMessage(Messages.system("你是助手"))
                .addMessage(Messages.user("你好"))
                .build();

        assertEquals(2, request.getMessages().size());
        assertEquals("你是助手", request.getMessages().get(0).getContent());
        assertEquals("你好", request.getMessages().get(1).getContent());
    }
}