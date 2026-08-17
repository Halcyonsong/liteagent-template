package io.github.halcyonsong.liteagent.memory.hook.chat;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.memory.hook.support.MemoryHookSupport;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

/**
 * Chat 侧记忆窗口 Hook。
 * <p>
 * 读取时机：INIT_WORKING_MESSAGES 之后，把历史消息拼到 workingMessages 前面。
 * 写回时机：END 之前，折叠本轮消息后追加到窗口尾部，并按 maxSize 裁剪。
 * <p>
 * 共享逻辑见 {@link MemoryHookSupport}。
 */
public class MemoryChatStepHook implements StepHook {

    private final MemoryWindowStore store;
    private final int maxSize;

    public MemoryChatStepHook(MemoryWindowStore store, int maxSize) {
        if (store == null) {
            throw new IllegalArgumentException("store must not be null");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.store = store;
        this.maxSize = maxSize;
    }

    public MemoryChatStepHook(MemoryWindowStore store) {
        this(store, 40);
    }

    @Override
    public void afterStep(ChatStepKey key, ChatAgentContext ctx, ChatStepKey nextKey) {
        if (!shouldLoadHistory(key)) {
            return;
        }
        MemoryHookSupport.loadHistory(store, ctx.getInvocation(), ctx.getWorkingMessages());
    }

    @Override
    public void beforeStep(ChatStepKey key, ChatAgentContext ctx) {
        if (!shouldSaveRound(key)) {
            return;
        }
        MemoryHookSupport.saveRound(store, ctx.getInvocation(), ctx.getWorkingMessages(), maxSize);
    }

    /**
     * 步骤匹配，可重写以适配自定义步骤链。
     */
    protected boolean shouldLoadHistory(ChatStepKey key) {
        return ChatStepKey.INIT_WORKING_MESSAGES.equals(key);
    }

    /**
     * 步骤匹配，可重写以适配自定义步骤链。
     */
    protected boolean shouldSaveRound(ChatStepKey key) {
        return ChatStepKey.END.equals(key);
    }
}
