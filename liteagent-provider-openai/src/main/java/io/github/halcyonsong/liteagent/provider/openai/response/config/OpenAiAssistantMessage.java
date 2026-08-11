package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;

import java.util.Collections;
import java.util.List;

/**
 * OpenAI-compatible provider 专属 assistant message。
 * <p>
 * 在统一的 assistant content 之外，额外保留 reasoning_content 和 tool_calls。
 */
public class OpenAiAssistantMessage extends AssistantMessage {

    private final String reasoningContent;
    private final List<OpenAiToolCall> toolCalls;

    public OpenAiAssistantMessage(String content,
                                  String reasoningContent,
                                  List<OpenAiToolCall> toolCalls) {
        super(content);
        this.reasoningContent = reasoningContent;
        this.toolCalls = toolCalls == null
                ? Collections.emptyList()
                : List.copyOf(toolCalls);
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public List<OpenAiToolCall> getToolCalls() {
        return toolCalls;
    }
}