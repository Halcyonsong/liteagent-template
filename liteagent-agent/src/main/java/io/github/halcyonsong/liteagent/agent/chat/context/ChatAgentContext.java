package io.github.halcyonsong.liteagent.agent.chat.context;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.Result;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChatAgentContext {

    private final String executionId;
    private final Invocation invocation;
    private final List<Message> workingMessages = new ArrayList<>();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Setter
    private Result result;

    @Setter
    private ToolRegistry toolRegistry;

    /**
     * 当前轮待写入 workingMessages 的 assistant 消息。
     */
    private final List<Message> pendingAssistantMessages = new ArrayList<>();

    /**
     * 当前轮待写入 workingMessages 的 tool 消息。
     */
    private final List<ToolMessage> pendingToolMessages = new ArrayList<>();

    private int iteration;
    private int maxIterations = 10;

    @Setter
    private AgentTerminationReason terminationReason;

    private ChatAgentContext(String executionId, Invocation invocation) {
        this.executionId = executionId;
        this.invocation = invocation;
    }

    public static ChatAgentContext create(Invocation invocation) {
        return create(UUID.randomUUID().toString(), invocation);
    }

    public static ChatAgentContext create(String executionId, Invocation invocation) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        return new ChatAgentContext(executionId, invocation);
    }

    public void clearWorkingMessages() {
        this.workingMessages.clear();
    }

    public void appendWorkingMessage(Message message) {
        this.workingMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    public void appendWorkingMessages(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.workingMessages.addAll(messages);
    }

    public void clearPendingMessages() {
        this.pendingAssistantMessages.clear();
        this.pendingToolMessages.clear();
    }

    public void appendPendingAssistantMessage(Message message) {
        this.pendingAssistantMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    public void appendPendingAssistantMessages(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.pendingAssistantMessages.addAll(messages);
    }

    public void appendPendingToolMessage(ToolMessage message) {
        this.pendingToolMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    public void appendPendingToolMessages(List<ToolMessage> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.pendingToolMessages.addAll(messages);
    }

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

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }

    public void incrementIteration() {
        this.iteration++;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }
        this.maxIterations = maxIterations;
    }
}