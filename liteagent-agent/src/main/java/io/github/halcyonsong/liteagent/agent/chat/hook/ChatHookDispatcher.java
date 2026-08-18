package io.github.halcyonsong.liteagent.agent.chat.hook;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class ChatHookDispatcher {

    private final List<StepHook> hooks;

    public ChatHookDispatcher(List<StepHook> hooks) {
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
    }

    public void beforeStep(ChatStepKey key, ChatAgentContext context) {
        for (StepHook hook : hooks) {
            safeBeforeStep(hook, key, context);
        }
    }

    public void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
        for (StepHook hook : hooks) {
            safeAfterStep(hook, key, context, nextKey);
        }
    }

    private void safeBeforeStep(StepHook hook, ChatStepKey key, ChatAgentContext context) {
        try {
            hook.beforeStep(key, context);
        } catch (Throwable error) {
            safeOnStepError(hook, key, context, error);
            log.warn("StepHook beforeStep failed. hook={}, step={}",
                    hook.getClass().getName(), key, error);
        }
    }

    private void safeAfterStep(StepHook hook,
                               ChatStepKey key,
                               ChatAgentContext context,
                               ChatStepKey nextKey) {
        try {
            hook.afterStep(key, context, nextKey);
        } catch (Throwable error) {
            safeOnStepError(hook, key, context, error);
            log.warn("StepHook afterStep failed. hook={}, step={}",
                    hook.getClass().getName(), key, error);
        }
    }

    public void onStepError(ChatStepKey key, ChatAgentContext context, Throwable error) {
        for (StepHook hook : hooks) {
            safeOnStepError(hook, key, context, error);
        }
    }

    private void safeOnStepError(StepHook hook, ChatStepKey key, ChatAgentContext context, Throwable error) {
        try {
            hook.onStepError(key, context, error);
        } catch (Throwable ignored) {
        }
    }
}