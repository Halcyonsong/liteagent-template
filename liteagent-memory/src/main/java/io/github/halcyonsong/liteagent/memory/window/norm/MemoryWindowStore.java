package io.github.halcyonsong.liteagent.memory.window.norm;

import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;

/**
 * 记忆窗口存储。按 sessionId 管理多个 {@link MemoryWindow} 实例。
 * 单例或多例由调用方决定：单实例全局共享，多实例各自隔离。
 */
public interface MemoryWindowStore {

    /** 获取或创建指定会话的记忆窗口。 */
    MemoryWindow getOrCreate(String sessionId);

    /** 删除指定会话的记忆窗口。 */
    void delete(String sessionId);

    /** 清空所有会话的记忆窗口。 */
    void clear();

    /**
     * 从持久化存储加载历史消息。内存场景无需重写；持久化场景重写以从数据库/文件读取。
     */
    default List<Message> loadHistory(String sessionId) {
        return List.of();
    }

    /**
     * 将窗口当前状态持久化到外部存储。框架不自动调用，开发者需显式调用。
     * 内存场景无需重写；持久化场景重写以写回数据库/文件。
     */
    default void persist(String sessionId, List<Message> messages) {
    }
}
