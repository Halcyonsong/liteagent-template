package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.halcyonsong.liteagent.core.model.request.RequestAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

/**
 * OpenAI-compatible chat completions 请求包装对象。
 * <p>
 * 该对象面向 provider 层使用，组合统一基础请求信息、统一聊天请求内容、
 * 通用聊天参数以及 OpenAI-compatible 协议扩展参数。
 * <p>
 * 该类本身不是直接发送的 JSON 请求体，实际发送时应映射为 raw request。
 */
@Getter
public class OpenAiChatCompletionRequest {

    private final BaseRequest baseRequest;
    private final ChatRequest chatRequest;
    /**
     * OpenAI-compatible 协议的扩展请求参数。
     * 该部分参数不属于 core 统一抽象范围。
     */
    private final OpenAiCompletionOptions completionOptions;

    /**
     * 请求增强器列表。
     */
    @JsonIgnore
    private final List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> advisors;

    private OpenAiChatCompletionRequest(Builder builder) {
        this.baseRequest = Objects.requireNonNull(builder.baseRequest, "baseRequest must not be null");
        this.chatRequest = Objects.requireNonNull(builder.chatRequest, "chatRequest must not be null");
        this.completionOptions = builder.completionOptions;
        this.advisors = builder.advisors == null
                ? List.of()
                : List.copyOf(builder.advisors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BaseRequest baseRequest;
        private ChatRequest chatRequest;
        private OpenAiCompletionOptions completionOptions;
        private List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> advisors;

        public Builder baseRequest(BaseRequest baseRequest) {
            this.baseRequest = baseRequest;
            return this;
        }

        public Builder chatRequest(ChatRequest chatRequest) {
            this.chatRequest = chatRequest;
            return this;
        }

        public Builder completionOptions(OpenAiCompletionOptions completionOptions) {
            this.completionOptions = completionOptions;
            return this;
        }

        public Builder advisor(RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest> advisor) {
            if (this.advisors == null) {
                this.advisors = new ArrayList<>();
            }
            this.advisors.add(advisor);
            return this;
        }

        public Builder advisors(List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> advisors) {
            this.advisors = advisors;
            return this;
        }

        public OpenAiChatCompletionRequest build() {
            return new OpenAiChatCompletionRequest(this);
        }
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiChatCompletionRequest{" +
                "baseUrl='" + baseRequest.getBaseUrl() + '\'' +
                ", model='" + baseRequest.getModel() + '\'' +
                ", messageCount=" + chatRequest.getMessages().size() +
                ", hasCompletionOptions=" + (completionOptions != null) +
                ", advisorCount=" + advisors.size() +
                '}';
    }

}