package io.github.halcyonsong.liteagent.agent.common.hook;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

/**
 * 步骤执行生命周期钩子。
 * <p>
 * 用于在不侵入具体步骤实现的前提下，
 * 为执行器统一接入日志、指标、追踪或审计逻辑。
 */
public interface StepHook {

    /**
     * 在步骤执行前触发。
     */
    default void beforeStep(ChatStepKey key, ChatAgentContext context) {
    }

    /**
     * 在步骤执行成功后触发。
     */
    default void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
    }

    /**
     * 在步骤执行抛出异常时触发。
     */
    default void onStepError(ChatStepKey key, ChatAgentContext context, Throwable error) {
    }
}