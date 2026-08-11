package io.github.halcyonsong.liteagent.core.message;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTypesTest {

    @Test
    void user_message_should_keep_role_and_content() {
        UserMessage message = new UserMessage("hello");

        assertEquals(MessageRole.USER, message.getRole());
        assertEquals("hello", message.getContent());
    }

    @Test
    void assistant_message_should_keep_role_and_content() {
        AssistantMessage message = new AssistantMessage("hi");

        assertEquals(MessageRole.ASSISTANT, message.getRole());
        assertEquals("hi", message.getContent());
    }

    @Test
    void system_message_should_keep_role_and_content() {
        SystemMessage message = new SystemMessage("system prompt");

        assertEquals(MessageRole.SYSTEM, message.getRole());
        assertEquals("system prompt", message.getContent());
    }

    @Test
    void tool_message_should_keep_role_and_content() {
        ToolMessage message = new ToolMessage("tool result");

        assertEquals(MessageRole.TOOL, message.getRole());
        assertEquals("tool result", message.getContent());
    }

    @Test
    void messages_constructor_should_create_expected_types() {
        assertEquals(MessageRole.USER, Messages.user("u").getRole());
        assertEquals(MessageRole.ASSISTANT, Messages.assistant("a").getRole());
        assertEquals(MessageRole.SYSTEM, Messages.system("s").getRole());
        assertEquals(MessageRole.TOOL, Messages.tool("t").getRole());
    }

    @Test
    void message_should_fail_when_content_is_null() {
        assertThrows(NullPointerException.class, () -> new UserMessage(null));
        assertThrows(NullPointerException.class, () -> new AssistantMessage(null));
        assertThrows(NullPointerException.class, () -> new SystemMessage(null));
        assertThrows(NullPointerException.class, () -> new ToolMessage(null));
    }
}