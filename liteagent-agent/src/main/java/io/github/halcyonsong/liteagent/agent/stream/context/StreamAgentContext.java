package io.github.halcyonsong.liteagent.agent.stream.context;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单次流式编排调用的上下文。
 * <p>
 * 该对象的生命周期限定在一次 execute 调用内部，
 * 不是全局共享状态，也不会跨请求复用。
 * <p>
 * 它主要承载六类数据：
 * 1. 本次调用输入 invocation
 * 2. 工作态消息历史 workingMessages，用于轮次间继续发给模型
 * 3. 每轮执行状态 rounds，用于记录响应聚合、工具调用和控制标志
 * 4. 执行控制状态：iteration、maxIterations、terminationReason
 * 5. 当前对外输出流 output
 * 6. 跨步骤共享的扩展数据 attributes
 * <p>
 * provider-specific 的中间产物不直接定义为固定字段，
 * 而是优先通过 rounds 或 attributes 扩展槽存储。
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
     * 本次流式编排的轮次历史。
     * <p>
     * 每进入一轮流式请求，都会创建一个新的 StreamRoundState 追加到该列表中。
     * 轮次索引从 0 开始。
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
}