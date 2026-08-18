package io.github.halcyonsong.liteagent.core.model.response.chat;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * 单个聊天响应内容对象。
 * <p>
 * 该对象用于封装一组消息内容。
 * 在当前设计中，单个 {@link ChatChoice} 内部持有一个 {@code ChatResponse}，
 * 从而保留“一条候选结果可对应多条消息”的扩展能力。
 */
@Getter
@ToString
public class ChatResponse implements JsonSerializable {

    /**
     * 当前响应内容包含的消息集合。
     */
    private final List<Message> messages;

    public ChatResponse(List<Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.messages = List.copyOf(messages);
    }

}