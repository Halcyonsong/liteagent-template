package io.github.halcyonsong.liteagent.core.model.response.chat;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * 单个聊天响应内容对象，封装一组消息。
 */
@Getter
@ToString
public class ChatResponse implements JsonSerializable {

    private final List<Message> messages;

    public ChatResponse(List<Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.messages = List.copyOf(messages);
    }

}