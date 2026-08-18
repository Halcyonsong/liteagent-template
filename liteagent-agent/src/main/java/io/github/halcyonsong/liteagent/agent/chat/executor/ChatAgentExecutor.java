package io.github.halcyonsong.liteagent.agent.chat.executor;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.hook.ChatHookDispatcher;
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
 */
public final class ChatAgentExecutor {

    private final Map<ChatStepKey, ChatStep> steps;
    private final ChatHookDispatcher hookDispatcher;
    private final int maxStepCount;
    private final int maxIterations;

    public ChatAgentExecutor(Map<ChatStepKey, ChatStep> steps) {
        this(steps, List.of(), 1000, 10);
    }

    public ChatAgentExecutor(Map<ChatStepKey, ChatStep> steps,
                             List<StepHook> hooks,
                             int maxStepCount) {
        this(steps, hooks, maxStepCount, 10);
    }

    public ChatAgentExecutor(Map<ChatStepKey, ChatStep> steps,
                             List<StepHook> hooks,
                             int maxStepCount,
                             int maxIterations) {
        Objects.requireNonNull(steps, "steps must not be null");
        if (maxStepCount < 1) {
            throw new IllegalArgumentException("maxStepCount must be greater than zero");
        }
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }
        this.steps = new HashMap<>(steps);
        this.hookDispatcher = new ChatHookDispatcher(hooks);
        this.maxStepCount = maxStepCount;
        this.maxIterations = maxIterations;
    }

    public ChatAgentContext execute(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");
        context.setMaxIterations(maxIterations);

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

            currentKey = invokeStep(currentKey, context);
        }

        invokeEndStep(context);
        return context;
    }

    private ChatStepKey invokeStep(ChatStepKey key, ChatAgentContext context) {
        ChatStep step = steps.get(key);
        if (step == null) {
            throw new IllegalStateException("No agent step registered for key: " + key);
        }


        try {
            hookDispatcher.beforeStep(key, context);

            ChatStepKey nextKey = Objects.requireNonNull(
                    step.invoke(context),
                    "agent step must return a next step"
            );

            hookDispatcher.afterStep(key, context, nextKey);
            return nextKey;
        } catch (Throwable error) {
            hookDispatcher.onStepError(key, context, error);
            throw error;
        }
    }

    private void invokeEndStep(ChatAgentContext context) {
        ChatStep endStep = steps.get(ChatStepKey.END);
        if (endStep == null) {
            throw new IllegalStateException("No agent step registered for key: " + ChatStepKey.END);
        }


        try {
            hookDispatcher.beforeStep(ChatStepKey.END, context);

            ChatStepKey nextKey = Objects.requireNonNull(
                    endStep.invoke(context),
                    "agent step must return a next step"
            );

            hookDispatcher.afterStep(ChatStepKey.END, context, nextKey);
        } catch (Throwable error) {
            hookDispatcher.onStepError(ChatStepKey.END, context, error);
            throw error;
        }
    }
}