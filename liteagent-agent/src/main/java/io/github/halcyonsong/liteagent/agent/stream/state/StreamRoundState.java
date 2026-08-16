package io.github.halcyonsong.liteagent.agent.stream.state;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 单轮流式请求的运行时状态。
 * <p>
 * 保存当前轮的增量聚合器、最终响应、待追加消息和附加属性。
 * 多轮历史由 StreamAgentContext 的 rounds 列表维护。
 */
@Getter
@Setter
public class StreamRoundState {

    /**
     * 当前轮索引，从 0 开始。
     */
    private final int roundIndex;

    /**
     * 当前轮是否已经完成。
     */
    private volatile boolean roundComplete;

    /**
     * 当前轮待写入 workingMessages 的 assistant 消息。
     */
    private final List<Message> pendingAssistantMessages = new ArrayList<>();

    /**
     * 当前轮待写入 workingMessages 的 tool 消息。
     */
    private final List<ToolMessage> pendingToolMessages = new ArrayList<>();

    /**
     * 当前轮增量聚合器。
     */
    private Object accumulator;

    /**
     * 当前轮聚合完成后的最终响应。
     */
    private Object finalResponse;

    /**
     * 当前轮附加状态。
     * <p>
     * 可用于存放 toolCalls、finishReason、usage、raw response metadata
     * 等当前轮特有的 provider-specific 数据。
     */
    private final Map<String, Object> attributes = new HashMap<>();

    public StreamRoundState(int roundIndex) {
        if (roundIndex < 0) {
            throw new IllegalArgumentException("roundIndex must not be negative");
        }
        this.roundIndex = roundIndex;
    }

    /**
     * 写入或覆盖当前轮附加属性。
     */
    public void setAttribute(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("attribute key must not be blank");
        }
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public void appendPendingAssistantMessage(Message message) {
        pendingAssistantMessages.add(
                Objects.requireNonNull(message, "message must not be null")
        );
    }

    public void appendPendingAssistantMessages(
            List<? extends Message> messages
    ) {
        Objects.requireNonNull(messages, "messages must not be null");
        pendingAssistantMessages.addAll(messages);
    }

    public void appendPendingToolMessage(ToolMessage message) {
        pendingToolMessages.add(
                Objects.requireNonNull(message, "message must not be null")
        );
    }

    public void appendPendingToolMessages(
            List<ToolMessage> messages
    ) {
        Objects.requireNonNull(messages, "messages must not be null");
        pendingToolMessages.addAll(messages);
    }

    public void clearPendingMessages() {
        pendingAssistantMessages.clear();
        pendingToolMessages.clear();
    }

    /**
     * 读取当前轮附加属性。
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 以强类型方式读取当前轮附加属性。
     */
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }
}