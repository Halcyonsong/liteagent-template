package io.github.halcyonsong.liteagent.memory.hook;

import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.memory.hook.chat.MemoryChatStepHook;
import io.github.halcyonsong.liteagent.memory.hook.stream.MemoryStreamStepHook;
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

/**
 * 记忆窗口 Hook 工厂。创建并注入 Chat 或 Stream Agent 的记忆 Hook。
 * 记忆窗口存储请通过 {@code MemoryWindows} 创建或获取。
 */
public final class MemoryHooks {

    private MemoryHooks() {
    }

    /** 创建使用全局共享内存 Store 的 Chat 记忆 Hook，默认最大消息数。 */
    public static StepHook chat() {
        return chat(MemoryWindows.shared());
    }

    /** 创建使用全局共享内存 Store 的 Chat 记忆 Hook。 */
    public static StepHook chat(int maxSize) {
        return chat(MemoryWindows.shared(), maxSize);
    }

    /** 创建 Chat Agent 记忆 Hook，默认最大消息数为 40。 */
    public static StepHook chat(MemoryWindowStore store) {
        return new MemoryChatStepHook(store);
    }

    /** 创建 Chat Agent 记忆 Hook。 */
    public static StepHook chat(MemoryWindowStore store, int maxSize) {
        return new MemoryChatStepHook(store, maxSize);
    }


    /** 创建使用全局共享内存 Store 的 Stream 记忆 Hook，默认最大消息数。 */
    public static StreamStepHook stream() {
        return stream(MemoryWindows.shared());
    }

    /** 创建使用全局共享内存 Store 的 Stream 记忆 Hook。 */
    public static StreamStepHook stream(int maxSize) {
        return stream(MemoryWindows.shared(), maxSize);
    }

    /** 创建 Stream Agent 记忆 Hook，默认最大消息数为 100。 */
    public static StreamStepHook stream(MemoryWindowStore store) {
        return new MemoryStreamStepHook(store);
    }

    /** 创建 Stream Agent 记忆 Hook。 */
    public static StreamStepHook stream(MemoryWindowStore store, int maxSize) {
        return new MemoryStreamStepHook(store, maxSize);
    }
}