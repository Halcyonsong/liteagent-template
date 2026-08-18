package io.github.halcyonsong.liteagent.agent.stream.context;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单次流式编排调用的上下文，只在一次 execute 调用内部使用，不跨请求复用。
 */
@Getter
public class StreamAgentContext<T> {

    private final String executionId;
    private final Invocation invocation;

    /** 只在第 0 轮初始化阶段从工具 advisor 中提取一次，后续轮次直接复用。 */
    @Setter
    private ToolRegistry toolRegistry;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Setter
    private Flux<T> output;

    /** 只在 INIT_WORKING_MESSAGES 步骤中从初始请求复制一次，后续轮次只追加。 */
    private final List<Message> workingMessages = new ArrayList<>();

    private final List<StreamRoundState> rounds = new ArrayList<>();

    private final AtomicInteger iteration = new AtomicInteger(0);

    private volatile int maxIterations = 10;

    private final AtomicInteger executedSteps = new AtomicInteger(0);

    @Setter
    private AgentTerminationReason terminationReason;

    private volatile boolean cancelled = false;

    /** 内部控制哨兵，用于驱动 expand / 轮次切换，不应暴露给外部订阅者。 */
    @Setter
    private volatile Object controlSignal;

    public boolean isControlSignal(Object value) {
        return controlSignal != null && controlSignal == value;
    }

    public void clearControlSignal() {
        this.controlSignal = null;
    }

    private StreamAgentContext(String executionId, Invocation invocation) {
        this.executionId = executionId;
        this.invocation = invocation;
    }

    public static <T> StreamAgentContext<T> create(Invocation invocation) {
        return create(UUID.randomUUID().toString(), invocation);
    }

    public static <T> StreamAgentContext<T> create(String executionId, Invocation invocation) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        return new StreamAgentContext<>(executionId, invocation);
    }

    public void clearWorkingMessages() {
        this.workingMessages.clear();
    }

    public void addRound(StreamRoundState roundState) {
        this.rounds.add(Objects.requireNonNull(roundState, "roundState must not be null"));
    }

    /** @throws IllegalStateException 当尚未创建任何轮次状态时 */
    public StreamRoundState currentRound() {
        if (rounds.isEmpty()) {
            throw new IllegalStateException("No stream round state exists");
        }
        return rounds.get(rounds.size() - 1);
    }

    public StreamRoundState getRound(int roundIndex) {
        return rounds.get(roundIndex);
    }

    public void appendWorkingMessage(Message message) {
        this.workingMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    public void appendWorkingMessages(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.workingMessages.addAll(messages);
    }

    /** 当 value 为 null 时删除对应键。 */
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

    public <V> V getAttribute(String key, Class<V> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }

    public int incrementIteration() {
        return iteration.incrementAndGet();
    }

    public int getIteration() {
        return iteration.get();
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }
        this.maxIterations = maxIterations;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public int incrementExecutedSteps() {
        return executedSteps.incrementAndGet();
    }

    public int getExecutedSteps() {
        return executedSteps.get();
    }

}