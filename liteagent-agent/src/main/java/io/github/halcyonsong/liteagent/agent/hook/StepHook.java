package io.github.halcyonsong.liteagent.agent.hook;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;

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
    default void beforeStep(AgentStepKey key, AgentContext context) {
    }

    /**
     * 在步骤执行成功后触发。
     */
    default void afterStep(AgentStepKey key, AgentContext context, AgentStepKey nextKey) {
    }

    /**
     * 在步骤执行抛出异常时触发。
     */
    default void onStepError(AgentStepKey key, AgentContext context, Throwable error) {
    }
}