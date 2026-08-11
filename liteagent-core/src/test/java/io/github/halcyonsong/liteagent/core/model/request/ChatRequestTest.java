package io.github.halcyonsong.liteagent.core.model.request;

import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatRequestTest {

    @Test
    void build_should_fail_when_messages_empty() {
        assertThrows(IllegalStateException.class, () -> ChatRequest.builder().build());
    }

    @Test
    void build_should_add_single_message() {
        ChatRequest request = ChatRequest.builder()
                .addMessage(new UserMessage("hello"))
                .build();

        assertEquals(1, request.getMessages().size());
        assertEquals("hello", request.getMessages().get(0).getContent());
    }

    @Test
    void build_should_add_multiple_messages() {
        ChatRequest request = ChatRequest.builder()
                .addMessages(List.of(
                        new UserMessage("first"),
                        new UserMessage("second")
                ))
                .build();

        assertEquals(2, request.getMessages().size());
        assertEquals("first", request.getMessages().get(0).getContent());
        assertEquals("second", request.getMessages().get(1).getContent());
    }

    @Test
    void add_message_should_fail_when_message_is_null() {
        assertThrows(NullPointerException.class, () ->
                ChatRequest.builder().addMessage(null));
    }

    @Test
    void add_messages_should_fail_when_messages_is_null() {
        assertThrows(NullPointerException.class, () ->
                ChatRequest.builder().addMessages(null));
    }

    @Test
    void get_messages_should_be_unmodifiable() {
        ChatRequest request = ChatRequest.builder()
                .addMessage(new UserMessage("hello"))
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                request.getMessages().add(new UserMessage("new")));
    }
}