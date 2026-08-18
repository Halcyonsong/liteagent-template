package io.github.halcyonsong.liteagent.agent.stream.hook;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class StreamHookDispatcher {

    private final List<StreamStepHook> hooks;

    public StreamHookDispatcher(List<StreamStepHook> hooks) {
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
    }

    public void beforeStep(StreamStepKey key, StreamAgentContext<?> context) {
        for (StreamStepHook hook : hooks) {
            safeBeforeStep(hook, key, context);
        }
    }

    public void afterStep(StreamStepKey key,
                          StreamAgentContext<?> context,
                          StreamStepKey nextKey) {
        for (StreamStepHook hook : hooks) {
            safeAfterStep(hook, key, context, nextKey);
        }
    }

    public void onStepError(StreamStepKey key,
                            StreamAgentContext<?> context,
                            Throwable error) {
        for (StreamStepHook hook : hooks) {
            safeOnStepError(hook, key, context, error);
        }
    }

    public void onStreamError(StreamAgentContext<?> context, Throwable error) {
        for (StreamStepHook hook : hooks) {
            safeOnStreamError(hook, context, error);
        }
    }

    public void onStreamComplete(StreamAgentContext<?> context) {
        for (StreamStepHook hook : hooks) {
            safeOnStreamComplete(hook, context);
        }
    }

    public void onStreamCancel(StreamAgentContext<?> context) {
        for (StreamStepHook hook : hooks) {
            safeOnStreamCancel(hook, context);
        }
    }

    private void safeBeforeStep(StreamStepHook hook,
                                StreamStepKey key,
                                StreamAgentContext<?> context) {
        try {
            hook.beforeStep(key, context);
        } catch (Throwable error) {
            log.warn("Stream hook beforeStep failed. hook={}, step={}",
                    hook.getClass().getName(), key, error);
            safeOnStepError(hook, key, context, error);
        }
    }

    private void safeAfterStep(StreamStepHook hook,
                               StreamStepKey key,
                               StreamAgentContext<?> context,
                               StreamStepKey nextKey) {
        try {
            hook.afterStep(key, context, nextKey);
        } catch (Throwable error) {
            log.warn("Stream hook afterStep failed. hook={}, step={}",
                    hook.getClass().getName(), key, error);
            safeOnStepError(hook, key, context, error);
        }
    }

    private void safeOnStepError(StreamStepHook hook,
                                 StreamStepKey key,
                                 StreamAgentContext<?> context,
                                 Throwable error) {
        try {
            hook.onStepError(key, context, error);
        } catch (Throwable callbackError) {
            log.warn("Stream hook onStepError failed. hook={}, step={}",
                    hook.getClass().getName(), key, callbackError);
        }
    }

    private void safeOnStreamError(StreamStepHook hook,
                                   StreamAgentContext<?> context,
                                   Throwable error) {
        try {
            hook.onStreamError(context, error);
        } catch (Throwable callbackError) {
            log.warn("Stream hook onStreamError failed. hook={}",
                    hook.getClass().getName(), callbackError);
        }
    }

    private void safeOnStreamComplete(StreamStepHook hook,
                                      StreamAgentContext<?> context) {
        try {
            hook.onStreamComplete(context);
        } catch (Throwable error) {
            log.warn("Stream hook onStreamComplete failed. hook={}",
                    hook.getClass().getName(), error);
        }
    }

    private void safeOnStreamCancel(StreamStepHook hook,
                                    StreamAgentContext<?> context) {
        try {
            hook.onStreamCancel(context);
        } catch (Throwable error) {
            log.warn("Stream hook onStreamCancel failed. hook={}",
                    hook.getClass().getName(), error);
        }
    }
}
