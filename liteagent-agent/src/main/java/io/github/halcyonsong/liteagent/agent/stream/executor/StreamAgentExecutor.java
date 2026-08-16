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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 step key 驱动的流式执行器。
 * <p>
 * 通过 expand 处理轮次展开；每次 buildNext 只消费一个已完成的 round，
 * 决定是否进入下一轮、追加消息或结束执行。
 */
public final class StreamAgentExecutor<T> {

    private final Map<StreamStepKey, StreamSyncStep> syncSteps;

    private final Map<StreamStepKey, StreamStep<Flux<T>>> streamSteps;

    private final List<StreamStepHook> hooks;

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

        this.syncSteps = new HashMap<>(syncSteps);
        this.streamSteps = new HashMap<>(streamSteps);
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
        this.maxStepCount = maxStepCount;
    }

    public StreamAgentContext<T> executeContext(StreamAgentContext<T> context) {
        Objects.requireNonNull(context, "context must not be null");
        context.setOutput(buildFlow(context));
        return context;
    }

    public Flux<T> execute(StreamAgentContext<T> context) {
        return executeContext(context).getOutput();
    }

    private Flux<T> buildFlow(StreamAgentContext<T> context) {
        return buildRound(context)
                .expand(chunk -> buildNext(context))
                .filter(chunk -> !context.isControlSignal(chunk))
                .doOnCancel(() -> context.setTerminationReason(AgentTerminationReason.CANCELLED));
    }

    private Flux<T> buildRound(StreamAgentContext<T> context) {
        if (context.isCancelled()) {
            context.setTerminationReason(AgentTerminationReason.CANCELLED);
            return Flux.empty();
        }

        StreamRoundState roundState = new StreamRoundState(context.getIteration());
        context.addRound(roundState);

        StreamStepKey syncKey = StreamStepKey.BEGIN;
        int executedSteps = 0;

        while (!syncKey.equals(StreamStepKey.SEND_REQUEST)) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            syncKey = invokeSyncStep(syncKey, context);
        }

        Flux<T> stream = null;
        StreamStepKey streamKey = StreamStepKey.SEND_REQUEST;

        while (!streamKey.equals(StreamStepKey.STREAM_END)) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("StreamAgent step limit exceeded: " + maxStepCount);
            }
            StreamApplyResult<Flux<T>> result = invokeStreamStep(streamKey, stream, context);
            stream = result.getOutput();
            streamKey = result.getNextKey();
        }

        return stream;
    }

    private Flux<T> buildNext(StreamAgentContext<T> context) {
        StreamRoundState roundState = context.currentRound();

        if (!roundState.isRoundComplete()) {
            return Flux.empty();
        }

        roundState.setRoundComplete(false);

        StreamStepKey nextAction = invokeSyncStep(
                StreamStepKey.DECIDE_NEXT_ACTION,
                context
        );

        StreamStepKey afterCurrentRound;

        if (nextAction.equals(StreamStepKey.EXECUTE_TOOL)) {
            afterCurrentRound = invokeSyncStep(
                    StreamStepKey.EXECUTE_TOOL,
                    context
            );
        } else {
            afterCurrentRound = nextAction;
        }

        if (afterCurrentRound.equals(StreamStepKey.APPEND_MESSAGES)) {
            afterCurrentRound = invokeSyncStep(
                    StreamStepKey.APPEND_MESSAGES,
                    context
            );
        }

        if (afterCurrentRound.equals(StreamStepKey.BUILD_RESULT)) {
            invokeSyncStep(StreamStepKey.BUILD_RESULT, context);
            return Flux.empty();
        }

        if (afterCurrentRound.equals(StreamStepKey.BEGIN)) {
            return buildRound(context);
        }

        if (afterCurrentRound.equals(StreamStepKey.END)) {
            return Flux.empty();
        }

        throw new IllegalStateException(
                "Unsupported next action after stream round: "
                        + afterCurrentRound
        );
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