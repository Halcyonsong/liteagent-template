package io.github.halcyonsong.liteagent.agent.chat.executor;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;

import java.util.HashMap;
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
 * 步骤注册表支持内置 key 和自定义 key，
 * 调用方可在构建注册表时插入自定义步骤。
 */
public final class ChatAgentExecutor {

    private final Map<ChatStepKey, ChatStep> steps;
    private final List<StepHook> hooks;
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
        this.steps = new HashMap<>(steps);
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
        this.maxStepCount = maxStepCount;
    }

    public ChatAgentContext execute(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        ChatStepKey currentKey = ChatStepKey.BEGIN;
        int executedSteps = 0;

        while (!currentKey.equals(ChatStepKey.END)) {
            if (context.isCancelled()) {
                context.setTerminationReason(AgentTerminationReason.CANCELLED);
                break;
            }

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