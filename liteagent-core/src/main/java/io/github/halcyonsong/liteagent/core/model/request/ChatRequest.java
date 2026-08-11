package io.github.halcyonsong.liteagent.core.model.request;

import io.github.halcyonsong.liteagent.core.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 统一的聊天请求内容对象。
 * <p>
 * 该对象仅负责封装本次对话输入的消息集合，
 * 不包含供应商地址、鉴权、模型名称等基础调用信息，
 * 也不包含供应商特有的协议扩展参数。
 */
public class ChatRequest {

    /**
     * 本次对话请求携带的消息集合，按发送顺序组织。
     */
    private final List<Message> messages;

    private ChatRequest(Builder builder) {
        this.messages = List.copyOf(builder.messages);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Message> messages = new ArrayList<>();

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