package io.github.halcyonsong.liteagent.memory.hook.support;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindow;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆窗口 Hook 共享逻辑。
 * <p>
 * Chat 和 Stream 两侧的 Hook 共享读取/写回/折叠逻辑，仅 context 类型不同。
 * 调用方负责从各自 context 取出 {@link Invocation} 和 workingMessages 传入。
 */
@Slf4j
public final class MemoryHookSupport {

    private MemoryHookSupport() {
    }

    /**
     * 读取历史消息并拼到 workingMessages 前面。
     *
     * @param store           记忆窗口存储
     * @param invocation      本次请求，用于获取 sessionId
     * @param workingMessages 工作消息列表，历史会被插入到头部
     */
    public static void loadHistory(MemoryWindowStore store, Invocation invocation, List<Message> workingMessages) {
        String sessionId = invocation.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        MemoryWindow window = store.getOrCreate(sessionId);
        List<Message> history = window.messages();
        if (!history.isEmpty()) {
            workingMessages.addAll(0, history);
        }
        log.debug(
                "Memory loaded. sessionId={}, historySize={}, workingSize={}",
                sessionId,
                history.size(),
                workingMessages.size()
        );
    }

    /**
     * 折叠本轮消息后追加到窗口尾部，并按 maxSize 裁剪。
     *
     * @param store           记忆窗口存储
     * @param invocation      本次请求，用于获取 sessionId
     * @param workingMessages 工作消息列表（最终状态）
     * @param maxSize         窗口消息上限
     */
    public static void saveRound(MemoryWindowStore store, Invocation invocation,
                                 List<Message> workingMessages, int maxSize) {
        String sessionId = invocation.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        MemoryWindow window = store.getOrCreate(sessionId);
        List<Message> folded = foldMessages(workingMessages);
        window.replaceAll(folded);
        while (window.size() > maxSize) {
            window.removeEarliest();
        }

        log.debug(
                "Memory saved. sessionId={}, foldedSize={}, windowSize={}",
                sessionId,
                folded.size(),
                window.size()
        );
    }

    /**
     * 折叠消息列表：
     * - system 消息跳过
     * - tool 消息跳过
     * - 连续 assistant 合并为一条（content 按顺序拼接，换行分隔）
     * - user 保留
     * <p>
     * 历史消息已经折叠过，线性扫描不会对它们产生重复合并。
     *
     * @param messages 原始消息列表
     * @return 折叠后的消息列表
     */
    public static List<Message> foldMessages(List<Message> messages) {
        List<Message> result = new ArrayList<>();
        StringBuilder pendingAssistant = new StringBuilder();

        for (Message message : messages) {
            if (message instanceof SystemMessage) {
                continue;
            }
            if (message instanceof UserMessage) {
                flushAssistant(result, pendingAssistant);
                result.add(message);
            } else if (message instanceof AssistantMessage) {
                String content = message.getContent();
                if (content != null && !content.isBlank()) {
                    if (!pendingAssistant.isEmpty()) {
                        pendingAssistant.append('\n');
                    }
                    pendingAssistant.append(content);
                }
            }
            // ToolMessage 跳过
        }
        flushAssistant(result, pendingAssistant);
        return result;
    }

    private static void flushAssistant(List<Message> result, StringBuilder pending) {
        if (!pending.isEmpty()) {
            result.add(new AssistantMessage(pending.toString()));
            pending.setLength(0);
        }
    }
}
