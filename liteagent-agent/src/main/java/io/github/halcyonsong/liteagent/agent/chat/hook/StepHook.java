package io.github.halcyonsong.liteagent.agent.chat.hook;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

/**
 * 步骤执行生命周期钩子，用于接入日志、指标、追踪或审计逻辑。
 */
public interface StepHook {

    default void beforeStep(ChatStepKey key, ChatAgentContext context) {
    }

    default void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
    }

    default void onStepError(ChatStepKey key, ChatAgentContext context, Throwable error) {
    }
}