package io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import lombok.Getter;

import java.util.Objects;

/**
 * OpenAI-compatible 快速构造入口。适合示例、测试或简单调用场景。
 */
@Getter
public class OpenAiQuickChatRequest implements JsonSerializable {

    /** 当前会话 ID，用于关联记忆窗口。允许为空。 */
    private final String sessionId;

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final String userMessage;
    private final String systemMessage;

    private OpenAiQuickChatRequest(Builder builder) {
        this.sessionId = builder.sessionId;
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey must not be null");
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
        this.userMessage = Objects.requireNonNull(builder.userMessage, "userMessage must not be null");
        this.systemMessage = builder.systemMessage;
    }

    /** 转换为完整的 OpenAI provider 基础请求对象。 */
    public OpenAiBaseRequest toOpenAiBaseRequest() {
        return OpenAiBaseRequest.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .build();
    }

    /** 转换为完整的聊天消息请求对象。 */
    public ChatRequest toChatRequest() {
        ChatRequest.Builder builder = ChatRequest.builder()
                .sessionId(sessionId);

        if (systemMessage != null && !systemMessage.isBlank()) {
            builder.addMessage(Messages.system(systemMessage));
        }

        builder.addMessage(Messages.user(userMessage));
        return builder.build();
    }

    /** 转换为完整的 OpenAI-compatible provider 请求对象。快速构造场景不含 completionOptions。 */
    public OpenAiChatCompletionRequest toChatCompletion() {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(toOpenAiBaseRequest())
                .chatRequest(toChatRequest())
                .completionOptions(null)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sessionId;
        private String baseUrl;
        private String apiKey;
        private String model;
        private String userMessage;
        private String systemMessage;

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public Builder systemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        public OpenAiQuickChatRequest build() {
            return new OpenAiQuickChatRequest(this);
        }
    }

    @Override
    public String toString() {
        return "OpenAiQuickChatRequest{" +
                "baseUrl='" + baseUrl + '\'' +
                ", apiKey='" + "****" + '\'' +
                ", model='" + model + '\'' +
                ", hasSystemMessage=" + (systemMessage != null && !systemMessage.isBlank()) +
                ", userMessageLength=" + (userMessage == null ? 0 : userMessage.length()) +
                '}';
    }
}