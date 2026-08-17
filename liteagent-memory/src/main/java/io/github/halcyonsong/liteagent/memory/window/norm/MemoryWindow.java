package io.github.halcyonsong.liteagent.memory.window.norm;

import io.github.halcyonsong.liteagent.core.message.norm.Message;

import java.util.List;
import java.util.Optional;

/**
 * 单个会话的记忆窗口。
 * <p>
 * 消息按时间顺序维护，最早消息在头部，最新消息在尾部。
 * <p>
 * 操作分四类：
 * <ul>
 *   <li>查看：只读不移除</li>
 *   <li>取出：移除并返回</li>
 *   <li>移除：只移除不返回</li>
 *   <li>追加：加到尾部</li>
 * </ul>
 * 实现应保证返回的 List 是不可变快照，避免外部直接修改窗口内部状态。
 */
public interface MemoryWindow {

    /**
     * 当前窗口所属会话 ID。
     */
    String sessionId();

    /**
     * 查看最早的消息，不移除。
     */
    Optional<Message> peekEarliest();

    /**
     * 查看最新的消息，不移除。
     */
    Optional<Message> peekLatest();

    /**
     * 获取窗口内全部消息的只读快照，按从早到晚排序，不移除窗口内容。
     * <p>
     * 返回不可变 List，调用方修改不影响窗口内部状态。
     *
     * @return 消息快照，空窗口返回空列表
     */
    List<Message> messages();

    /**
     * 取出最早的消息，移除并返回。
     */
    Optional<Message> pollEarliest();

    /**
     * 取出最新的消息，移除并返回。
     */
    Optional<Message> pollLatest();

    /**
     * 移除最早的消息，不返回。
     */
    void removeEarliest();

    /**
     * 移除最新的消息，不返回。
     */
    void removeLatest();

    /**
     * 清空当前会话窗口。
     */
    void clear();

    /**
     * 追加单条消息到窗口尾部。
     */
    void append(Message message);

    /**
     * 按传入顺序批量追加消息到窗口尾部。
     */
    void appendAll(List<? extends Message> messages);

    /**
     * 当前窗口内的消息数量。
     */
    int size();

    /**
     * 当前窗口是否为空。
     */
    default boolean isEmpty() {
        return size() == 0;
    }
}
