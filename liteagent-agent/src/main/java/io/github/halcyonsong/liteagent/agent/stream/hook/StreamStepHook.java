package io.github.halcyonsong.liteagent.agent.stream.hook;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;

/**
 * 流式步骤生命周期钩子。
 * <p>
 * 用于接入日志、metrics、trace 或 checkpoint 等横切逻辑。
 */
public interface StreamStepHook {

    default void beforeStep(StreamStepKey key, StreamAgentContext<?> context) {
    }

    default void afterStep(StreamStepKey key, StreamAgentContext<?> context, StreamStepKey nextKey) {
    }

    default void onStepError(StreamStepKey key, StreamAgentContext<?> context, Throwable error) {
    }

    default void onStreamError(StreamAgentContext<?> context, Throwable error) {
    }

    default void onStreamComplete(StreamAgentContext<?> context) {
    }

    default void onStreamCancel(StreamAgentContext<?> context) {
    }
}