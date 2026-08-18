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
    private final int maxSessions;
    private final long idleTimeoutMillis;
    private final ConcurrentMap<String, Long> lastAccessTimes = new ConcurrentHashMap<>();

    private static final int DEFAULT_MAX_SESSIONS = 1_000;
    private static final long DEFAULT_IDLE_TIMEOUT_MILLIS = 30 * 60 * 1_000L;

    public InMemoryMemoryWindowStore() {
        this(DEFAULT_MAX_SESSIONS, DEFAULT_IDLE_TIMEOUT_MILLIS);
    }

    public InMemoryMemoryWindowStore(int maxSessions, long idleTimeoutMillis) {
        if (maxSessions <= 0) {
            throw new IllegalArgumentException("maxSessions must be greater than zero");
        }
        if (idleTimeoutMillis <= 0) {
            throw new IllegalArgumentException("idleTimeoutMillis must be greater than zero");
        }

        this.maxSessions = maxSessions;
        this.idleTimeoutMillis = idleTimeoutMillis;
    }

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

        long now = System.currentTimeMillis();
        cleanupExpired(now);
        lastAccessTimes.put(sessionId, now);

        MemoryWindow existing = windows.get(sessionId);
        if (existing != null) {
            return existing;
        }

        // 缓存未命中：这里不持有 ConcurrentHashMap 的内部锁，可以安全执行慢 I/O。
        List<Message> history = loadHistory(sessionId);
        MemoryWindow candidate = new InMemoryMemoryWindow(sessionId);
        if (history != null && !history.isEmpty()) {
            candidate.appendAll(history);
        }

        // 多线程同时首次访问同一 session 时，只有一个 candidate 会成功发布。
        MemoryWindow raced = windows.putIfAbsent(sessionId, candidate);
        if (raced != null) {
            return raced;
        }

        log.debug(
                "Memory window created. sessionId={}, historySize={}",
                sessionId,
                history == null ? 0 : history.size()
        );

        evictIfNecessary();
        return candidate;
    }

    @Override
    public void delete(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        windows.remove(sessionId);
        lastAccessTimes.remove(sessionId);
    }

    @Override
    public void clear() {
        windows.clear();
        lastAccessTimes.clear();
    }

    private void cleanupExpired(long now) {
        for (ConcurrentMap.Entry<String, Long> entry : lastAccessTimes.entrySet()) {
            String sessionId = entry.getKey();
            Long lastAccessTime = entry.getValue();

            if (now - lastAccessTime < idleTimeoutMillis) {
                continue;
            }

            /*
             * remove(key, value) 确保不会删除在本次检查期间
             * 已被其他请求重新访问的 session。
             */
            if (lastAccessTimes.remove(sessionId, lastAccessTime)) {
                windows.remove(sessionId);
            }
        }
    }

    private void evictIfNecessary() {
        while (windows.size() > maxSessions) {
            int sizeBeforeEviction = windows.size();
            removeOldestSession();

            // 避免极端竞争下无法移除时出现无限循环。
            if (windows.size() >= sizeBeforeEviction) {
                return;
            }
        }
    }

    private void removeOldestSession() {
        ConcurrentMap.Entry<String, Long> oldest = null;

        for (ConcurrentMap.Entry<String, Long> entry : lastAccessTimes.entrySet()) {
            if (oldest == null || entry.getValue() < oldest.getValue()) {
                oldest = entry;
            }
        }

        if (oldest == null) {
            return;
        }

        String sessionId = oldest.getKey();
        Long accessTime = oldest.getValue();

        if (lastAccessTimes.remove(sessionId, accessTime)) {
            windows.remove(sessionId);
        }
    }

}
