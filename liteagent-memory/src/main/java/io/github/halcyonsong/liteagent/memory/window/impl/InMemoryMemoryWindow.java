package io.github.halcyonsong.liteagent.memory.window.impl;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;

import java.util.*;

/**
 * 基于内存 Deque 的记忆窗口实现。
 * <p>
 * 队列头部保存最早消息，队列尾部保存最新消息。
 * 所有操作通过 synchronized 保证线程安全。
 */
public final class InMemoryMemoryWindow implements MemoryWindow {

    private final String sessionId;
    private final Deque<Message> messages = new ArrayDeque<>();

    public InMemoryMemoryWindow(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        this.sessionId = sessionId;
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public synchronized Optional<Message> peekEarliest() {
        return Optional.ofNullable(messages.peekFirst());
    }

    @Override
    public synchronized Optional<Message> peekLatest() {
        return Optional.ofNullable(messages.peekLast());
    }

    @Override
    public synchronized List<Message> messages() {
        return List.copyOf(messages);
    }

    @Override
    public synchronized Optional<Message> pollEarliest() {
        return Optional.ofNullable(messages.pollFirst());
    }

    @Override
    public synchronized Optional<Message> pollLatest() {
        return Optional.ofNullable(messages.pollLast());
    }

    @Override
    public synchronized void removeEarliest() {
        messages.pollFirst();
    }

    @Override
    public synchronized void removeLatest() {
        messages.pollLast();
    }

    @Override
    public synchronized void clear() {
        messages.clear();
    }

    @Override
    public synchronized void append(Message message) {
        Objects.requireNonNull(message, "message must not be null");
        messages.addLast(message);
    }

    @Override
    public synchronized void appendAll(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        messages.forEach(message ->
                Objects.requireNonNull(message, "message must not be null")
        );
        this.messages.addAll(messages);
    }

    @Override
    public synchronized int size() {
        return messages.size();
    }
}
