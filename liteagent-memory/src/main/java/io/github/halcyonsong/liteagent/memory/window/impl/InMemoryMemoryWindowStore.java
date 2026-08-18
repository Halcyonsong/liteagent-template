package io.github.halcyonsong.liteagent.memory.window.impl;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存 ConcurrentHashMap 的记忆窗口存储实现。使用实例字段 Map，支持多实例隔离。
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
     * 获取或创建指定会话的记忆窗口。不存在时创建并加载历史，可重写 {@link #loadHistory(String)}。
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

        log.debug("Window created. session={}, history={}",
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

            /* remove(key, value) 确保不删除检查期间被重新访问的 session。 */
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
