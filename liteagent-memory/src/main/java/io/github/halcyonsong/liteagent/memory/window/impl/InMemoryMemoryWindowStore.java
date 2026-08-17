package io.github.halcyonsong.liteagent.memory.window.impl;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存 ConcurrentHashMap 的记忆窗口存储实现。
 * <p>
 * 使用实例字段 Map，支持创建多个独立隔离的 Store 实例。
 */
@Slf4j
public class InMemoryMemoryWindowStore implements MemoryWindowStore {

    private final ConcurrentMap<String, MemoryWindow> windows = new ConcurrentHashMap<>();

    /**
     * 获取或创建指定会话 ID 的记忆窗口。
     * <p>
     * 如果会话 ID 不存在，创建新窗口并加载历史记录。
     * 可以重写 {@link #loadHistory(String)} 方法，从外部存储加载历史记录。
     * @param sessionId 会话唯一标识
     * @return 记忆窗口实例
     */
    @Override
    public MemoryWindow getOrCreate(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        return windows.computeIfAbsent(sessionId, id -> {
            MemoryWindow window = new InMemoryMemoryWindow(id);
            List<Message> history = loadHistory(id);
            if (history != null && !history.isEmpty()) {
                window.appendAll(history);
            }

            log.debug(
                    "Memory window created. sessionId={}, historySize={}",
                    id,
                    history == null ? 0 : history.size()
            );

            return window;
        });
    }

    @Override
    public void delete(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        windows.remove(sessionId);
    }
}
