package io.github.halcyonsong.liteagent.memory.window.norm;

import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;

/**
 * 记忆窗口存储。
 * <p>
 * 负责按 sessionId 管理多个 {@link MemoryWindow} 实例。
 * 接口不约束单例或多例，由调用方决定持有方式：
 * <ul>
 *   <li>单实例：应用全局创建一次，所有调用方共享</li>
 *   <li>多实例：各自独立隔离，相同 sessionId 在不同 Store 里是不同窗口</li>
 * </ul>
 */
public interface MemoryWindowStore {

    /**
     * 获取或创建指定会话的记忆窗口。
     *
     * @param sessionId 会话唯一标识
     * @return 该会话对应的记忆窗口，若不存在则创建
     */
    MemoryWindow getOrCreate(String sessionId);

    /**
     * 删除指定会话的记忆窗口。
     *
     * @param sessionId 会话唯一标识
     */
    void delete(String sessionId);

    /**
     * 从持久化存储加载指定会话的历史消息。
     * <p>
     * 内存场景无需重写，返回空列表。
     * 持久化场景重写此方法，从数据库/文件等读取历史记录。
     *
     * @param sessionId 会话唯一标识
     * @return 历史消息列表，空列表表示无历史或新建会话
     */
    default List<Message> loadHistory(String sessionId) {
        return List.of();
    }

    /**
     * 将窗口当前状态持久化到外部存储。
     * <p>
     * 框架不会自动调用此方法，开发者需要在合适的时机（如请求结束后）显式调用。
     * 内存场景无需重写，空操作。
     * 持久化场景重写此方法，将 messages 写回数据库/文件等外部存储。
     *
     * @param sessionId 会话唯一标识
     * @param messages 窗口当前消息快照（只读）
     */
    default void persist(String sessionId, List<Message> messages) {
    }
}
