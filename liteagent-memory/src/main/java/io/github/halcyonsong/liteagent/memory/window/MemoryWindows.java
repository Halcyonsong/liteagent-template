package io.github.halcyonsong.liteagent.memory.window;

import io.github.halcyonsong.liteagent.memory.window.impl.InMemoryMemoryWindowStore;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;

/**
 * 记忆窗口工厂入口。
 * <p>
 * 提供两种创建方式：
 * <ul>
 *   <li>{@link #inMemory()}：显式创建独立实例，支持多实例隔离</li>
 *   <li>{@link #shared()}：全局懒加载单例，不想管生命周期的场景使用</li>
 * </ul>
 */
public final class MemoryWindows {

    private MemoryWindows() {
    }

    /**
     * 创建一个独立的内存记忆窗口存储实例。
     * <p>
     * 每次调用返回新实例，各自隔离，适合测试隔离或多租户场景。
     *
     * @return 新的内存存储实例
     */
    public static MemoryWindowStore inMemory() {
        return new InMemoryMemoryWindowStore();
    }

    /**
     * 获取全局共享的内存记忆窗口存储实例。
     * <p>
     * 使用静态内部类懒加载，线程安全。应用全局只初始化一次，
     * 所有调用方共享同一个 Store 实例。
     *
     * @return 全局共享的内存存储实例
     */
    public static MemoryWindowStore shared() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 全局共享的内存记忆窗口存储实例。
     */
    private static final class LazyHolder {
        static final MemoryWindowStore INSTANCE = new InMemoryMemoryWindowStore();
    }
}
