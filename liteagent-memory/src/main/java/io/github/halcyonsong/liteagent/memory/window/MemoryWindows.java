package io.github.halcyonsong.liteagent.memory.window;

import io.github.halcyonsong.liteagent.memory.window.impl.InMemoryMemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

/**
 * 记忆窗口工厂入口。{@link #inMemory()} 创建独立实例，{@link #shared()} 获取全局懒加载单例。
 */
public final class MemoryWindows {

    private MemoryWindows() {
    }

    /** 创建独立内存存储实例，每次返回新实例，适合测试隔离或多租户场景。 */
    public static MemoryWindowStore inMemory() {
        return new InMemoryMemoryWindowStore();
    }

    /** 获取全局共享内存存储实例（静态内部类懒加载，线程安全）。 */
    public static MemoryWindowStore shared() {
        return LazyHolder.INSTANCE;
    }

    private static final class LazyHolder {
        static final MemoryWindowStore INSTANCE = new InMemoryMemoryWindowStore();
    }
}
