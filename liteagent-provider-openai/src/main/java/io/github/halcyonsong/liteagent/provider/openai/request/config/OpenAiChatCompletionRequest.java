package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;

import java.util.Objects;

/**
 * OpenAI-compatible chat completions 请求包装对象。
 * <p>
 * 该对象面向 provider 层使用，组合统一基础请求信息、统一聊天请求内容、
 * 通用聊天参数以及 OpenAI-compatible 协议扩展参数。
 * <p>
 * 该类本身不是直接发送的 JSON 请求体，实际发送时应映射为 raw request。
 */
public class OpenAiChatCompletionRequest {

    private final BaseRequest baseRequest;
    private final ChatRequest chatRequest;
    private final ChatOptions chatOptions;
    /**
     * OpenAI-compatible 协议的扩展请求参数。
     * 该部分参数不属于 core 统一抽象范围。
     */
    private final OpenAiCompletionOptions completionOptions;

    private OpenAiChatCompletionRequest(Builder builder) {
        this.baseRequest = Objects.requireNonNull(builder.baseRequest, "baseRequest must not be null");
        this.chatRequest = Objects.requireNonNull(builder.chatRequest, "chatRequest must not be null");
        this.chatOptions = builder.chatOptions;
        this.completionOptions = builder.completionOptions;
    }

    public BaseRequest getBaseRequest() {
        return baseRequest;
    }

    public ChatRequest getChatRequest() {
        return chatRequest;
    }

    public ChatOptions getChatOptions() {
        return chatOptions;
    }

    public OpenAiCompletionOptions getCompletionOptions() {
        return completionOptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BaseRequest baseRequest;
        private ChatRequest chatRequest;
        private ChatOptions chatOptions;
        private OpenAiCompletionOptions completionOptions;

        public Builder baseRequest(BaseRequest baseRequest) {
            this.baseRequest = baseRequest;
            return this;
        }

        public Builder chatRequest(ChatRequest chatRequest) {
            this.chatRequest = chatRequest;
            return this;
        }

        public Builder chatOptions(ChatOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }

        public Builder completionOptions(OpenAiCompletionOptions completionOptions) {
            this.completionOptions = completionOptions;
            return this;
        }

        public OpenAiChatCompletionRequest build() {
            return new OpenAiChatCompletionRequest(this);
        }
    }
}