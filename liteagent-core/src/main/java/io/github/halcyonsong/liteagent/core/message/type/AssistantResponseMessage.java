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
 * 在基础 assistant content 之外，补充 reasoningContent 与 toolCalls，
 * 作为统一层对模型响应消息的增强规范。
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