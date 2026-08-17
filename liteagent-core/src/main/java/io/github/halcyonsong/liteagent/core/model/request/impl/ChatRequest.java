package io.github.halcyonsong.liteagent.core.model.request.impl;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 统一的聊天请求内容对象。
 * <p>
 * 仅封装消息序列，不包含基础调用信息和 provider 扩展参数。
 */
@Getter
public class ChatRequest {

    /**
     * 当前会话 ID。
     * 作为会话的唯一标识，用于关联记忆窗口。
     * 允许为空，不使用记忆窗口。
     */
    private final String sessionId;

    /**
     * 本次对话请求携带的消息集合，按发送顺序组织。
     */
    private final List<Message> messages;

    private ChatRequest(Builder builder) {
        this.sessionId = builder.sessionId;
        this.messages = List.copyOf(builder.messages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String sessionId;
        private final List<Message> messages = new ArrayList<>();

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder addMessage(Message message) {
            this.messages.add(Objects.requireNonNull(message, "message must not be null"));
            return this;
        }

        public Builder addMessages(List<Message> messages) {
            Objects.requireNonNull(messages, "messages must not be null");
            for (Message message : messages) {
                addMessage(message);
            }
            return this;
        }

        public ChatRequest build() {
            if (messages.isEmpty()) {
                throw new IllegalStateException("messages must not be empty");
            }
            return new ChatRequest(this);
        }
    }
}