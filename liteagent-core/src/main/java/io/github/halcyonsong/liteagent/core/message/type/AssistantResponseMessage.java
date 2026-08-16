package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 完整 assistant 响应消息。
 * <p>
 * 作为 chat 和 stream 的统一响应载体，
 * 扩展保留 reasoningContent 和 toolCalls。
 */
@Getter
@ToString(callSuper = true)
public class AssistantResponseMessage extends AssistantMessage {

    private final String reasoningContent;
    private final List<ToolCall> toolCalls;

    public AssistantResponseMessage(String content) {
        this(content, null, Collections.emptyList());
    }

    public AssistantResponseMessage(
            String content,
            String reasoningContent,
            List<ToolCall> toolCalls
    ) {
        super(content);
        this.reasoningContent = reasoningContent;
        this.toolCalls = toolCalls == null
                ? Collections.emptyList()
                : List.copyOf(toolCalls);
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

}