package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssistantResponseMessageTest {

    @Test
    void should_create_with_content_only() {
        AssistantResponseMessage msg = new AssistantResponseMessage("hello");

        assertEquals(MessageRole.ASSISTANT, msg.getRole());
        assertEquals("hello", msg.getContent());
        assertNull(msg.getReasoningContent());
        assertTrue(msg.getToolCalls().isEmpty());
    }

    @Test
    void should_create_with_reasoning_and_tool_calls() {
        List<ToolCall> toolCalls = List.of(
                new ToolCall(0, "call_1", "function",
                        new FunctionCall("get_weather", "{\"city\":\"北京\"}"))
        );

        AssistantResponseMessage msg = new AssistantResponseMessage(
                "北京晴天",
                "用户询问天气，需要调用工具",
                toolCalls
        );

        assertEquals("北京晴天", msg.getContent());
        assertEquals("用户询问天气，需要调用工具", msg.getReasoningContent());
        assertEquals(1, msg.getToolCalls().size());
        assertEquals("get_weather", msg.getToolCalls().get(0).getFunction().getName());
    }

    @Test
    void should_default_null_tool_calls_to_empty_list() {
        AssistantResponseMessage msg = new AssistantResponseMessage("content", null, null);

        assertTrue(msg.getToolCalls().isEmpty());
        assertNull(msg.getReasoningContent());
    }

    @Test
    void should_be_immutable_tool_calls() {
        AssistantResponseMessage msg = new AssistantResponseMessage(
                "content", null, List.of(new ToolCall(0, "c1", "function", new FunctionCall("f", "{}")))
        );

        List<ToolCall> toolCalls = msg.getToolCalls();
        assertThrows(UnsupportedOperationException.class, () -> toolCalls.add(null));
    }

    @Test
    void should_extend_assistant_message() {
        AssistantResponseMessage msg = new AssistantResponseMessage("content");

        assertInstanceOf(AssistantMessage.class, msg);
    }

    @Test
    void should_serialize_to_json() {
        AssistantResponseMessage msg = new AssistantResponseMessage("hello", "thinking", List.of());

        String json = msg.toCompactJson();
        assertNotNull(json);
        assertTrue(json.contains("hello"));
        assertTrue(json.contains("thinking"));
    }
}
