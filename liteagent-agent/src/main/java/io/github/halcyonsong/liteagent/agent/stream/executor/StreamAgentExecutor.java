package io.github.halcyonsong.liteagent.agent.stream.executor;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamHookDispatcher;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 step key 驱动的流式执行器。
 */
public final class StreamAgentExecutor<T> {

    private final Map<StreamStepKey, StreamSyncStep> syncSteps;
    private final Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps;
    private final StreamHookDispatcher hookDispatcher;
    private final int maxStepCount;
    private final int maxIterations;

    public StreamAgentExecutor(Map<StreamStepKey, StreamSyncStep> syncSteps,
                               Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps) {
        this(syncSteps, streamSteps, List.of(), 1000, 10);
    }

    public StreamAgentExecutor(Map<StreamStepKey, StreamSyncStep> syncSteps,
                               Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps,
                               List<StreamStepHook> hooks,
                               int maxStepCount) {
        this(syncSteps, streamSteps, hooks, maxStepCount, 10);
    }

    public StreamAgentExecutor(Map<StreamStepKey, StreamSyncStep> syncSteps,
                               Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps,
                               List<StreamStepHook> hooks,
                               int maxStepCount,
                               int maxIterations) {
        Objects.requireNonNull(syncSteps, "syncSteps must not be null");
        Objects.requireNonNull(streamSteps, "streamSteps must not be null");
        if (maxStepCount < 1) {
            throw new IllegalArgumentException("maxStepCount must be greater than zero");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }

        this.syncSteps = new HashMap<>(syncSteps);
        this.streamSteps = new HashMap<>(streamSteps);
        this.hookDispatcher = new StreamHookDispatcher(hooks);
        this.maxStepCount = maxStepCount;
        this.maxIterations = maxIterations;
    }

    public StreamAgentContext<T> executeContext(StreamAgentContext<T> context) {
        Objects.requireNonNull(context, "context must not be null");
        context.setMaxIterations(maxIterations);
        context.setOutput(buildFlow(context));
        return context;
    }

    public Flux<T> execute(StreamAgentContext<T> context) {
        return executeContext(context).getOutput();
    }

    private Flux<T> buildFlow(StreamAgentContext<T> context) {
        return Flux.defer(() -> buildRound(context))
                .expand(chunk -> buildNext(context))
                .filter(chunk -> !context.isControlSignal(chunk))
                .doOnError(error -> hookDispatcher.onStreamError(context, error))
                .doOnComplete(() -> hookDispatcher.onStreamComplete(context))
                .doOnCancel(() -> {
                    context.setTerminationReason(AgentTerminationReason.CANCELLED);
                    hookDispatcher.onStreamCancel(context);
                });
    }

    private Flux<T> buildRound(StreamAgentContext<T> context) {
        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        StreamRoundState roundState = new StreamRoundState(context.getIteration());
        context.addRound(roundState);

        StreamStepKey syncKey = StreamStepKey.BEGIN;

        while (!syncKey.equals(StreamStepKey.SEND_REQUEST)) {
            if (context.isCancelled()) {
                context.setTerminationReason(AgentTerminationReason.CANCELLED);
                return Flux.empty();
            }
            if (context.incrementExecutedSteps() > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            syncKey = invokeSyncStep(syncKey, context);
        }

        Flux<T> stream = null;
        StreamStepKey streamKey = StreamStepKey.SEND_REQUEST;

        while (!streamKey.equals(StreamStepKey.STREAM_END)) {
            if (context.isCancelled()) {
                context.setTerminationReason(AgentTerminationReason.CANCELLED);
                return Flux.empty();
            }
            if (context.incrementExecutedSteps() > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            StreamApplyResult<Flux<T>> result = invokeStreamStep(streamKey, stream, context);
            stream = result.getOutput();
            streamKey = result.getNextKey();
        }

        return stream;
    }

    private Flux<T> buildNext(StreamAgentContext<T> context) {
        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        StreamRoundState roundState = context.currentRound();

        if (!roundState.isRoundComplete()) {
            return Flux.empty();
        }

        roundState.setRoundComplete(false);

        StreamStepKey nextAction = invokeSyncStep(StreamStepKey.DECIDE_NEXT_ACTION, context);

        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        if (!nextAction.equals(StreamStepKey.EXECUTE_TOOL)) {
            // 无需工具执行，直接走后续逻辑（纯内存操作）
            return buildNextAfterTool(context, nextAction);
        }

        // 工具执行可能阻塞，隔离到 boundedElastic 线程池
        return Mono.fromCallable(() -> invokeSyncStep(StreamStepKey.EXECUTE_TOOL, context))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(afterTool -> buildNextAfterTool(context, afterTool));
    }

    private Flux<T> buildNextAfterTool(StreamAgentContext<T> context, StreamStepKey afterCurrentRound) {
        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        if (afterCurrentRound.equals(StreamStepKey.APPEND_MESSAGES)) {
            afterCurrentRound = invokeSyncStep(StreamStepKey.APPEND_MESSAGES, context);
        }

        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        if (afterCurrentRound.equals(StreamStepKey.BUILD_RESULT)) {
            invokeSyncStep(StreamStepKey.BUILD_RESULT, context);
            invokeSyncStep(StreamStepKey.END, context);
            return Flux.empty();
        }

        if (afterCurrentRound.equals(StreamStepKey.BEGIN)) {
            return buildRound(context);
        }

        if (afterCurrentRound.equals(StreamStepKey.END)) {
            invokeSyncStep(StreamStepKey.END, context);
            return Flux.empty();
        }

        throw new IllegalStateException("Unsupported next action after stream round: " + afterCurrentRound);
    }

    private StreamStepKey invokeSyncStep(StreamStepKey key, StreamAgentContext<T> context) {
        StreamSyncStep step = syncSteps.get(key);
        if (step == null) {
            throw new IllegalStateException("No stream sync step registered for key: " + key);
        }


        try {
            hookDispatcher.beforeStep(key, context);

            StreamStepKey nextKey = Objects.requireNonNull(
                    step.invoke(context),
                    "stream sync step must return a next step"
            );

            hookDispatcher.afterStep(key, context, nextKey);
            return nextKey;
        } catch (Throwable error) {
            hookDispatcher.onStepError(key, context, error);
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
            hookDispatcher.beforeStep(key, context);

            StreamApplyResult<Flux<T>> result = Objects.requireNonNull(
                    step.apply(upstream, context),
                    "stream step must return StreamApplyResult"
            );

            hookDispatcher.afterStep(key, context, result.getNextKey());
            return result;
        } catch (Throwable error) {
            hookDispatcher.onStepError(key, context, error);
            throw error;
        }
    }
}