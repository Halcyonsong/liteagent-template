package io.github.halcyonsong.liteagent.memory.window.norm;

import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;
import java.util.Optional;

/**
 * 单个会话的记忆窗口。消息按时间顺序维护，最早在头部，最新在尾部。
 * 实现应保证返回的 List 是不可变快照，避免外部直接修改窗口内部状态。
 */
public interface MemoryWindow {

    String sessionId();

    /** 查看最早的消息，不移除。 */
    Optional<Message> peekEarliest();

    /** 查看最新的消息，不移除。 */
    Optional<Message> peekLatest();

    /** 全部消息的只读快照，按从早到晚排序，不可变。 */
    List<Message> messages();

    /** 取出最早的消息，移除并返回。 */
    Optional<Message> pollEarliest();

    /** 取出最新的消息，移除并返回。 */
    Optional<Message> pollLatest();

    /** 移除最早的消息，不返回。 */
    void removeEarliest();

    /** 移除最新的消息，不返回。 */
    void removeLatest();

    /** 清空窗口。 */
    void clear();

    /** 追加单条消息到尾部。 */
    void append(Message message);

    /** 按传入顺序批量追加消息到尾部。 */
    void appendAll(List<? extends Message> messages);

    /**
     * 原子替换窗口全部消息。等效于 clear + appendAll 但作为单次原子操作，
     * 不会产生中间状态（空窗口）。
     */
    void replaceAll(List<? extends Message> messages);

    /** 当前窗口消息数量。 */
    int size();

    /** 当前窗口是否为空。 */
    default boolean isEmpty() {
        return size() == 0;
    }
}
