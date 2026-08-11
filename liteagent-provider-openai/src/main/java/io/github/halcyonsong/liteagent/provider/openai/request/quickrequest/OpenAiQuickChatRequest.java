package io.github.halcyonsong.liteagent.provider.openai.request.quickrequest;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;

import java.util.Objects;

/**
 * OpenAI-compatible 快速聊天请求构造对象。
 * <p>
 * 面向快速测试和最小调用场景，只要求提供 baseUrl、apiKey、model、
 * userMessage 和可选的 systemMessage，内部自动封装为完整的 ChatInvocation。
 */
public class OpenAiQuickChatRequest {

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final String userMessage;
    private final String systemMessage;

    private OpenAiQuickChatRequest(Builder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey must not be null");
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
        this.userMessage = Objects.requireNonNull(builder.userMessage, "userMessage must not be null");
        this.systemMessage = builder.systemMessage;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    /**
     * 转换为完整的 OpenAI provider 基础请求对象。
     */
    public OpenAiBaseRequest toBaseRequest() {
        return OpenAiBaseRequest.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .build();
    }

    /**
     * 转换为完整的聊天消息请求对象。
     */
    public ChatRequest toChatRequest() {
        ChatRequest.Builder builder = ChatRequest.builder();

        if (systemMessage != null && !systemMessage.isBlank()) {
            builder.addMessage(Messages.system(systemMessage));
        }

        builder.addMessage(Messages.user(userMessage));
        return builder.build();
    }

    /**
     * 转换为完整的统一调用对象。
     * <p>
     * 当前快速构造场景不包含 ChatOptions，默认使用 null。
     */
    public ChatInvocation toInvocation() {
        return ChatInvocation.builder()
                .baseRequest(toBaseRequest())
                .chatRequest(toChatRequest())
                .chatOptions(null)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl;
        private String apiKey;
        private String model;
        private String userMessage;
        private String systemMessage;

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

    public String toJson() {
        return JsonSupport.toJson(this);
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