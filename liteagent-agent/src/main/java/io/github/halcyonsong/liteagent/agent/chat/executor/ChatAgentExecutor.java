package io.github.halcyonsong.liteagent.agent.chat.executor;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.common.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 step key 顺序推进的同步 chat 执行器。
 * <p>
 * 执行器维护步骤注册表，并从 BEGIN 开始逐步执行。
 * 每个步骤返回下一个要执行的 ChatStepKey，
 * 执行器据此推进整个 chat 编排流程，直到 END。
 * <p>
 * 当前实现只支持单链路推进，
 * 不处理并行分支、多后继节点或流式调度。
 */
public final class ChatAgentExecutor {

    /**
     * 步骤注册表。
     * <p>
     * key 为步骤标识，value 为对应的步骤实现。
     */
    private final Map<ChatStepKey, ChatStep> steps;
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

    public ChatAgentExecutor(Map<ChatStepKey, ChatStep> steps) {
        this(steps, List.of(), 1000);
    }

    public ChatAgentExecutor(Map<ChatStepKey, ChatStep> steps,
                             List<StepHook> hooks,
                             int maxStepCount) {
        Objects.requireNonNull(steps, "steps must not be null");
        if (maxStepCount < 1) {
            throw new IllegalArgumentException("maxStepCount must be greater than zero");
        }
        this.steps = new EnumMap<>(ChatStepKey.class);
        this.steps.putAll(steps);
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
        this.maxStepCount = maxStepCount;
    }

    /**
     * 执行一次完整的同步 chat 编排。
     * <p>
     * 执行从 BEGIN 开始，
     * 每个步骤执行完成后返回下一步，直到返回 END。
     *
     * @param context 本次执行上下文
     * @return 执行完成后的上下文
     */
    public ChatAgentContext execute(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        ChatStepKey currentKey = ChatStepKey.BEGIN;
        int executedSteps = 0;

        while (currentKey != ChatStepKey.END) {
            if (++executedSteps > maxStepCount) {
                throw new IllegalStateException("ChatAgent step limit exceeded: " + maxStepCount);
            }

            ChatStepKey stepKey = currentKey;
            ChatStep step = steps.get(stepKey);
            if (step == null) {
                throw new IllegalStateException("No agent step registered for key: " + stepKey);
            }

            try {
                hooks.forEach(hook -> hook.beforeStep(stepKey, context));

                ChatStepKey nextKey = Objects.requireNonNull(
                        step.invoke(context),
                        "agent step must return a next step"
                );

                hooks.forEach(hook -> hook.afterStep(stepKey, context, nextKey));
                currentKey = nextKey;
            } catch (Throwable error) {
                hooks.forEach(hook -> hook.onStepError(stepKey, context, error));
                throw error;
            }
        }

        return context;
    }
}