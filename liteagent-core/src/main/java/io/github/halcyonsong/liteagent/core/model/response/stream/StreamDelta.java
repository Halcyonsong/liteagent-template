package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;

/**
 * 流式响应中的增量消息片段。
 * <p>
 * 该对象对应 OpenAI-compatible 协议中的 delta 结构，
 * 用于承载一次流式 chunk 中新增的角色信息、文本内容等。
 */
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

    public StreamDelta(String role, String content, String reasoningContent) {
        this.role = role;
        this.content = content;
        this.reasoningContent = reasoningContent;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    @Override
    public String toString() {
        return "StreamDelta{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                ", reasoningContent='" + reasoningContent + '\'' +
                '}';
    }
}