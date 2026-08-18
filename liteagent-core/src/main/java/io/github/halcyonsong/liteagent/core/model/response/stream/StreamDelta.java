package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 流式响应中的增量片段。toolCalls 需要由上层按 index 聚合。
 */
@Getter
@ToString
public class StreamDelta implements JsonSerializable {

    private final String role;

    /** 流式返回中通常为部分内容，可能为空字符串或 null。 */
    private final String content;

    /** 用于展示模型的推理过程，可能为空字符串或 null。 */
    private final String reasoningContent;

    /** 流式场景下为工具调用的增量片段，需要由上层按 index 聚合。 */
    private final List<ToolCall> toolCalls;

    public StreamDelta(String role, String content, String reasoningContent, List<ToolCall> toolCalls) {
        this.role = role;
        this.content = content;
        this.reasoningContent = reasoningContent;
        this.toolCalls = toolCalls == null ? Collections.emptyList() : List.copyOf(toolCalls);
    }

}