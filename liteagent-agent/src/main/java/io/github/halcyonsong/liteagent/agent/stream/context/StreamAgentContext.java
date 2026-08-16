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

/**
 * 单次流式编排调用的上下文。
 * <p>
 * 该对象只在一次 execute 调用内部使用，不跨请求复用。
 * 它承载输入、工作态消息、轮次状态、扩展属性、输出流和执行控制信号。
 */
@Getter
public class StreamAgentContext<T> {

    /**
     * 本次调用的唯一执行标识。
     */
    private final String executionId;

    /**
     * 本次调用的统一输入对象。
     */
    private final Invocation invocation;

    /**
     * 当前编排可用的工具注册表。
     * <p>
     * 只在第 0 轮初始化阶段从工具 advisor 中提取一次，
     * 后续轮次直接复用。
     */
    @Setter
    private ToolRegistry toolRegistry;

    /**
     * 跨步骤共享的扩展数据槽。
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * 当前对外输出的流式结果。
     */
    @Setter
    private Flux<T> output;

    /**
     * 本次流式编排内部使用的工作态消息历史。
     * <p>
     * 该列表只在 INIT_WORKING_MESSAGES 步骤中从初始请求复制一次，
     * 后续轮次只追加新的 assistant、tool 或其他工作消息。
     */
    private final List<Message> workingMessages = new ArrayList<>();

    /**
     * 本次流式编排的轮次历史，从第 0 轮开始，按执行顺序追加。
     */
    private final List<StreamRoundState> rounds = new ArrayList<>();

    /**
     * 当前已经进入的模型调用轮次。
     */
    private int iteration;

    /**
     * 允许的最大模型调用轮次。
     */
    private int maxIterations = 10;

    /**
     * 本次执行的结束原因。
     */
    @Setter
    private AgentTerminationReason terminationReason;

    private volatile boolean cancelled = false;

    /**
     * 内部控制哨兵，用于驱动 expand / 轮次切换。
     * 不应作为业务数据暴露给外部订阅者。
     */
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

    /**
     * 追加一轮新的流式状态。
     *
     * @param roundState 当前轮状态对象
     */
    public void addRound(StreamRoundState roundState) {
        this.rounds.add(Objects.requireNonNull(roundState, "roundState must not be null"));
    }

    /**
     * 获取当前轮状态。
     * <p>
     * 当前轮始终是 rounds 列表中的最后一个元素。
     *
     * @return 当前轮状态
     * @throws IllegalStateException 当尚未创建任何轮次状态时抛出
     */
    public StreamRoundState currentRound() {
        if (rounds.isEmpty()) {
            throw new IllegalStateException("No stream round state exists");
        }
        return rounds.get(rounds.size() - 1);
    }

    public StreamRoundState getRound(int roundIndex) {
        return rounds.get(roundIndex);
    }

    /**
     * 向工作态消息历史追加一条消息。
     */
    public void appendWorkingMessage(Message message) {
        this.workingMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    /**
     * 向工作态消息历史追加多条消息。
     */
    public void appendWorkingMessages(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.workingMessages.addAll(messages);
    }

    /**
     * 写入或覆盖一个扩展属性。
     * <p>
     * 当 value 为 null 时，会删除对应键。
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

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public <V> V getAttribute(String key, Class<V> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }

    /**
     * 将当前执行轮次加一。
     */
    public void incrementIteration() {
        this.iteration++;
    }

    /**
     * 设置允许的最大模型调用轮次。
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }
        this.maxIterations = maxIterations;
    }

    public void cancel() {
        this.cancelled = true;
    }

}