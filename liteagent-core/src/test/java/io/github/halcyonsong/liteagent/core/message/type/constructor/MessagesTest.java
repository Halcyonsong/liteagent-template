package io.github.halcyonsong.liteagent.core.message.type.constructor;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void should_create_system_message() {
        SystemMessage msg = Messages.system("system prompt");

        assertEquals(MessageRole.SYSTEM, msg.getRole());
        assertEquals("system prompt", msg.getContent());
    }

    @Test
    void should_create_user_message() {
        UserMessage msg = Messages.user("hello");

        assertEquals(MessageRole.USER, msg.getRole());
        assertEquals("hello", msg.getContent());
    }

    @Test
    void should_create_assistant_message() {
        AssistantMessage msg = Messages.assistant("hi");

        assertEquals(MessageRole.ASSISTANT, msg.getRole());
        assertEquals("hi", msg.getContent());
    }

    @Test
    void should_create_assistant_response_with_content_only() {
        AssistantResponseMessage msg = Messages.assistantResponse("result");

        assertEquals(MessageRole.ASSISTANT, msg.getRole());
        assertEquals("result", msg.getContent());
        assertNull(msg.getReasoningContent());
        assertTrue(msg.getToolCalls().isEmpty());
    }

    @Test
    void should_create_assistant_response_with_full_fields() {
        List<ToolCall> toolCalls = List.of(
                new ToolCall(0, "call_1", "function",
                        new FunctionCall("get_weather", "{\"city\":\"北京\"}"))
        );

        AssistantResponseMessage msg = Messages.assistantResponse(
                "北京晴天",
                "需要调用天气工具",
                toolCalls
        );

        assertEquals("北京晴天", msg.getContent());
        assertEquals("需要调用天气工具", msg.getReasoningContent());
        assertEquals(1, msg.getToolCalls().size());
    }

    @Test
    void should_create_tool_message() {
        ToolMessage msg = Messages.tool("{\"temp\": 28}", "call_001");

        assertEquals(MessageRole.TOOL, msg.getRole());
        assertEquals("{\"temp\": 28}", msg.getContent());
        assertEquals("call_001", msg.getToolCallId());
    }
}
