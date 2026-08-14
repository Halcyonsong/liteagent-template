package io.github.halcyonsong.liteagent.agent.executor;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于步骤队列的同步执行器。
 * <p>
 * 执行器维护步骤注册表，并按队列顺序依次执行步骤。
 * 每个步骤返回下一个要执行的 {@link io.github.halcyonsong.liteagent.agent.step.AgentStepKey}，
 * 执行器据此推进整个 agent 流程。
 * <p>
 * 当前最小实现只支持单步骤顺序推进，
 * 不处理分支并发，也不内置流式回环调度。
 */
public final class AgentExecutor {

    /**
     * 步骤注册表。
     * <p>
     * key 为步骤标识，value 为对应的步骤实现。
     */
    private final Map<AgentStepKey, AgentStep> steps;
    /**
     * 步骤生命周期钩子。
     * <p>
     * 可用于接入日志、metrics、trace 或 checkpoint 等横切逻辑。
     */
    private final List<StepHook> hooks;
    /**
     * 单次执行允许的最大步骤数。
     * <p>
     * 用于避免步骤回环配置错误导致的无限执行。
     */
    private final int maxStepCount;

    public AgentExecutor(Map<AgentStepKey, AgentStep> steps) {
        this(steps, List.of(), 1000);
    }

    public AgentExecutor(Map<AgentStepKey, AgentStep> steps,
                         List<StepHook> hooks,
                         int maxStepCount) {
        Objects.requireNonNull(steps, "steps must not be null");
        if (maxStepCount < 1) {
            throw new IllegalArgumentException("maxStepCount must be greater than zero");
        }
        this.steps = new EnumMap<>(AgentStepKey.class);
        this.steps.putAll(steps);
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
        this.maxStepCount = maxStepCount;
    }

    /**
     * 执行一次完整的步骤队列。
     * <p>
     * 执行从 {@link io.github.halcyonsong.liteagent.agent.step.AgentStepKey#BEGIN} 开始，
     * 每个步骤执行完成后返回下一步骤，直到返回 {@code END} 或队列排空。
     *
     * @param context 本次执行上下文
     * @return 执行完成后的上下文
     */
    public AgentContext execute(AgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Deque<AgentStepKey> queue = new ArrayDeque<>();
        queue.offer(AgentStepKey.BEGIN);

        int executedSteps = 0;
        while (!queue.isEmpty()) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("Agent step limit exceeded: " + maxStepCount);
            }

            AgentStepKey key = queue.poll();
            AgentStep step = steps.get(key);
            if (step == null) {
                throw new IllegalStateException("No agent step registered for key: " + key);
            }

            try {
                hooks.forEach(hook -> hook.beforeStep(key, context));
                AgentStepKey nextKey = Objects.requireNonNull(step.invoke(context), "agent step must return a next step");
                hooks.forEach(hook -> hook.afterStep(key, context, nextKey));

                if (nextKey != AgentStepKey.END) {
                    queue.offer(nextKey);
                }
            } catch (Throwable error) {
                hooks.forEach(hook -> hook.onStepError(key, context, error));
                throw error;
            }
        }

        return context;
    }
}