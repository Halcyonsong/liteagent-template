package io.github.halcyonsong.liteagent.core.message.type.constructor;

import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;

import java.util.List;

public final class Messages {

    private Messages() {
    }

    public static UserMessage user(String content) {
        return new UserMessage(content);
    }

    public static AssistantMessage assistant(String content) {
        return new AssistantMessage(content);
    }

    public static AssistantResponseMessage assistantResponse(String content) {
        return new AssistantResponseMessage(content);
    }

    public static AssistantResponseMessage assistantResponse(
            String content,
            String reasoningContent,
            List<ToolCall> toolCalls
    ) {
        return new AssistantResponseMessage(content, reasoningContent, toolCalls);
    }

    public static SystemMessage system(String content) {
        return new SystemMessage(content);
    }

    public static ToolMessage tool(String content, String toolCallId) {
        return new ToolMessage(content, toolCallId);
    }
}