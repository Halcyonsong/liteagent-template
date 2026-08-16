package io.github.halcyonsong.liteagent.core.message;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTypesTest {

    @Test
    void shouldCreateSystemMessage() {
        SystemMessage message = new SystemMessage("system prompt");
        assertEquals(MessageRole.SYSTEM, message.getRole());
        assertEquals("system prompt", message.getContent());
    }

    @Test
    void shouldCreateUserMessage() {
        UserMessage message = new UserMessage("hello");
        assertEquals(MessageRole.USER, message.getRole());
        assertEquals("hello", message.getContent());
    }

    @Test
    void shouldCreateAssistantMessage() {
        AssistantMessage message = new AssistantMessage("hi");
        assertEquals(MessageRole.ASSISTANT, message.getRole());
        assertEquals("hi", message.getContent());
    }

    @Test
    void shouldCreateToolMessage() {
        ToolMessage message = new ToolMessage("tool output", "tool-call-id");
        assertEquals(MessageRole.TOOL, message.getRole());
        assertEquals("tool output", message.getContent());
    }
}