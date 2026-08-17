package io.github.halcyonsong.liteagent.memory.hook.stream;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.memory.hook.support.MemoryHookSupport;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

/**
 * Stream 侧记忆窗口 Hook。
 * <p>
 * 读取时机：INIT_WORKING_MESSAGES 之后，把历史消息拼到 workingMessages 前面。
 * 写回时机：END 之前，折叠本轮消息后追加到窗口尾部，并按 maxSize 裁剪。
 * <p>
 * 共享逻辑见 {@link MemoryHookSupport}。
 * <p>
 * 边界场景：Stream 侧下游 cancel 时 END 不一定触发，本轮消息可能不写回。
 * 这是可接受语义——取消意味着用户主动中断，本轮消息不完整。
 */
public class MemoryStreamStepHook implements StreamStepHook {

    private final MemoryWindowStore store;
    private final int maxSize;

    public MemoryStreamStepHook(MemoryWindowStore store, int maxSize) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.store = store;
        this.maxSize = maxSize;
    }

    public MemoryStreamStepHook(MemoryWindowStore store) {
        this(store, 40);
    }

    @Override
    public void afterStep(StreamStepKey key, StreamAgentContext<?> ctx, StreamStepKey nextKey) {
        if (!shouldLoadHistory(key)) {
            return;
        }
        MemoryHookSupport.loadHistory(store, ctx.getInvocation(), ctx.getWorkingMessages());
    }

    @Override
    public void beforeStep(StreamStepKey key, StreamAgentContext<?> ctx) {
        if (!shouldSaveRound(key)) {
            return;
        }
        MemoryHookSupport.saveRound(store, ctx.getInvocation(), ctx.getWorkingMessages(), maxSize);
    }

    /**
     * 步骤匹配，可重写以适配自定义步骤链。
     */
    protected boolean shouldLoadHistory(StreamStepKey key) {
        return StreamStepKey.INIT_WORKING_MESSAGES.equals(key);
    }

    /**
     * 步骤匹配，可重写以适配自定义步骤链。
     */
    protected boolean shouldSaveRound(StreamStepKey key) {
        return StreamStepKey.END.equals(key);
    }
}
