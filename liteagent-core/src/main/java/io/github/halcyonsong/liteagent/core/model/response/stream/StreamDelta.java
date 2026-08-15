package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 流式响应中的增量消息片段。
 * <p>
 * 该对象对应 OpenAI-compatible 协议中的 delta 结构，
 * 用于承载一次流式 chunk 中新增的角色信息、文本内容、推理内容以及工具调用增量。
 */
@Getter
@ToString
public class StreamDelta {

    /**
     * 当前增量片段的消息角色。
     * 例如 assistant。
     */
    private final String role;

    /**
     * 当前增量片段新增的文本内容。
     * <p>
     * 在流式返回中通常为部分内容，可能为空字符串或 null。
     */
    private final String content;

    /**
     * 当前增量片段新增的推理内容。
     * <p>
     * 用于展示模型的推理过程，可能为空字符串或 null。
     */
    private final String reasoningContent;

    /**
     * 当前增量片段中的工具调用信息。
     * <p>
     * 在流式场景下，该字段通常表现为工具调用的增量片段，
     * 后续需要由上层按 index 做聚合。
     */
    private final List<ToolCall> toolCalls;

    public StreamDelta(
            String role,
            String content,
            String reasoningContent,
            List<ToolCall> toolCalls
    ) {
        this.role = role;
        this.content = content;
        this.reasoningContent = reasoningContent;
        this.toolCalls = toolCalls == null
                ? Collections.emptyList()
                : List.copyOf(toolCalls);
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}