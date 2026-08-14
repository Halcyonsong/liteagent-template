package io.github.halcyonsong.liteagent.agent.stream.executor;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import reactor.core.publisher.Flux;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 step key 驱动的流式执行器。
 * <p>
 * 执行器分为三个阶段：
 * 1. Phase 1：同步准备阶段，顺序推进到 SEND_REQUEST
 * 2. Phase 2：流式管道构建阶段，顺序包装上游流直到 STREAM_END
 * 3. Phase 3：轮次调度阶段，通过 expand 在单轮结束后决定是否进入下一轮
 * <p>
 * 该执行器只负责流式编排流程和轮次调度，
 * 不直接解析 provider chunk 内部结构；
 * 具体的 chunk 增量分析、响应聚合和工具调用判断应由 provider-specific 的流步骤完成。
 *
 * @param <T> provider 对外输出的单个流元素类型
 */
public final class StreamAgentExecutor<T> {

    /**
     * 服务于流式编排的同步步骤注册表。
     */
    private final Map<StreamStepKey, StreamSyncStep> syncSteps;

    /**
     * 服务于流式编排的流步骤注册表。
     * <p>
     * 每个步骤处理的都是 Flux<T>，并返回处理后的 Flux<T>。
     * 这些步骤通常由 provider 层提供，用于执行 chunk 增强、增量聚合和轮次分析。
     */
    private final Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps;

    /**
     * 流步骤生命周期钩子。
     */
    private final List<StreamStepHook> hooks;

    /**
     * 单次执行允许的最大步骤数。
     */
    private final int maxStepCount;

    public StreamAgentExecutor(Map<StreamStepKey, StreamSyncStep> syncSteps,
                               Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps) {
        this(syncSteps, streamSteps, List.of(), 1000);
    }

    public StreamAgentExecutor(Map<StreamStepKey, StreamSyncStep> syncSteps,
                               Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps,
                               List<StreamStepHook> hooks,
                               int maxStepCount) {
        Objects.requireNonNull(syncSteps, "syncSteps must not be null");
        Objects.requireNonNull(streamSteps, "streamSteps must not be null");
        if (maxStepCount < 1) {
            throw new IllegalArgumentException("maxStepCount must be greater than zero");
        }

        this.syncSteps = new EnumMap<>(StreamStepKey.class);
        this.syncSteps.putAll(syncSteps);

        this.streamSteps = new EnumMap<>(StreamStepKey.class);
        this.streamSteps.putAll(streamSteps);

        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
        this.maxStepCount = maxStepCount;
    }

    /**
     * 执行一次完整的流式编排准备，并将惰性输出流写入上下文。
     * <p>
     * 该方法返回时不会主动消费流；
     * 真正执行发生在调用方订阅 output 时。
     */
    public StreamAgentContext<T> executeContext(StreamAgentContext<T> context) {
        Objects.requireNonNull(context, "context must not be null");
        context.setOutput(buildFlow(context));
        return context;
    }

    /**
     * 执行一次完整的流式编排，并直接返回 provider 对外输出的流。
     *
     * @param context 本次流式编排上下文
     * @return provider 对外输出的流
     */
    public Flux<T> execute(StreamAgentContext<T> context) {
        return executeContext(context).getOutput();
    }

    /**
     * 构建完整的多轮流式输出。
     * <p>
     * 先构建第一轮流，再通过 expand 在轮次完成后决定是否进入下一轮。
     */
    private Flux<T> buildFlow(StreamAgentContext<T> context) {
        return buildRound(context)
                .expand(chunk -> buildNext(context));
    }

    /**
     * 构建单轮流式请求的完整输出。
     * <p>
     * 在进入当前轮时，会先创建并登记新的 StreamRoundState。
     * <p>
     * Phase 1：同步准备
     * Phase 2：流式管道构建
     */
    private Flux<T> buildRound(StreamAgentContext<T> context) {
        StreamRoundState roundState = new StreamRoundState(context.getIteration());
        context.addRound(roundState);

        StreamStepKey syncKey = StreamStepKey.BEGIN;
        int executedSteps = 0;

        while (syncKey != StreamStepKey.SEND_REQUEST) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            syncKey = invokeSyncStep(syncKey, context);
        }

        Flux<T> stream = null;
        StreamStepKey streamKey = StreamStepKey.SEND_REQUEST;

        while (streamKey != StreamStepKey.STREAM_END) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            StreamApplyResult<Flux<T>> result = invokeStreamStep(streamKey, stream, context);
            stream = result.getOutput();
            streamKey = result.getNextKey();
        }

        return stream;
    }

    /**
     * 在当前轮输出的每个元素到达后，检查是否需要展开下一轮。
     * <p>
     * 实际上只有当前轮已经完成时，才会真正触发后续同步决策逻辑。
     */
    private Flux<T> buildNext(StreamAgentContext<T> context) {
        StreamRoundState roundState = context.currentRound();
        if (!roundState.isRoundComplete()) {
            return Flux.empty();
        }

        roundState.setRoundComplete(false);

        StreamStepKey nextAction = invokeSyncStep(StreamStepKey.DECIDE_NEXT_ACTION, context);

        if (nextAction == StreamStepKey.BUILD_RESULT) {
            invokeSyncStep(StreamStepKey.BUILD_RESULT, context);
            return Flux.empty();
        }

        if (nextAction == StreamStepKey.EXECUTE_TOOL) {
            if (context.getIteration() >= context.getMaxIterations()) {
                context.setTerminationReason(AgentTerminationReason.MAX_ITERATIONS_REACHED);
                invokeSyncStep(StreamStepKey.BUILD_RESULT, context);
                return Flux.empty();
            }

            context.incrementIteration();
            invokeSyncStep(StreamStepKey.EXECUTE_TOOL, context);
            return buildRound(context);
        }

        if (nextAction == StreamStepKey.END) {
            return Flux.empty();
        }

        throw new IllegalStateException("Unsupported next action after stream round: " + nextAction);
    }

    private StreamStepKey invokeSyncStep(StreamStepKey key, StreamAgentContext<T> context) {
        StreamSyncStep step = syncSteps.get(key);
        if (step == null) {
            throw new IllegalStateException("No stream sync step registered for key: " + key);
        }

        try {
            hooks.forEach(hook -> hook.beforeStep(key, context));

            StreamStepKey nextKey = Objects.requireNonNull(
                    step.invoke(context),
                    "stream sync step must return a next step"
            );

            hooks.forEach(hook -> hook.afterStep(key, context, nextKey));
            return nextKey;
        } catch (Throwable error) {
            hooks.forEach(hook -> hook.onStepError(key, context, error));
            throw error;
        }
    }

    private StreamApplyResult<Flux<T>> invokeStreamStep(StreamStepKey key,
                                                        Flux<T> upstream,
                                                        StreamAgentContext<T> context) {
        StreamStep<Flux<T>> step = streamSteps.get(key);
        if (step == null) {
            throw new IllegalStateException("No stream step registered for key: " + key);
        }

        try {
            hooks.forEach(hook -> hook.beforeStep(key, context));

            StreamApplyResult<Flux<T>> result = Objects.requireNonNull(
                    step.apply(upstream, context),
                    "stream step must return StreamApplyResult"
            );

            hooks.forEach(hook -> hook.afterStep(key, context, result.getNextKey()));
            return result;
        } catch (Throwable error) {
            hooks.forEach(hook -> hook.onStepError(key, context, error));
            throw error;
        }
    }
}