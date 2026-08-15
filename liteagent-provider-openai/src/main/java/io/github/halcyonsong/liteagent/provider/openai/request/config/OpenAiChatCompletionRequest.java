package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.norm.ResponseAdvisor;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
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
public class OpenAiChatCompletionRequest implements Invocation {

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
    private final List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> requestAdvisors;

    /**
     * 普通对话响应增强器列表。
     */
    @JsonIgnore
    private final List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiChatCompletionResponse>> chatResponseAdvisors;

    /**
     * 流式响应增强器列表。
     */
    @JsonIgnore
    private final List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiStreamCompletionResponse>> streamResponseAdvisors;

    private OpenAiChatCompletionRequest(Builder builder) {
        this.baseRequest = Objects.requireNonNull(builder.baseRequest, "baseRequest must not be null");
        this.chatRequest = Objects.requireNonNull(builder.chatRequest, "chatRequest must not be null");
        this.completionOptions = builder.completionOptions;
        this.requestAdvisors = builder.requestAdvisors == null
                ? List.of()
                : List.copyOf(builder.requestAdvisors);
        this.chatResponseAdvisors = builder.chatResponseAdvisors == null
                ? List.of()
                : List.copyOf(builder.chatResponseAdvisors);
        this.streamResponseAdvisors = builder.streamResponseAdvisors == null
                ? List.of()
                : List.copyOf(builder.streamResponseAdvisors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BaseRequest baseRequest;
        private ChatRequest chatRequest;
        private OpenAiCompletionOptions completionOptions;
        private List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> requestAdvisors;
        private List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiChatCompletionResponse>> chatResponseAdvisors;
        private List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiStreamCompletionResponse>> streamResponseAdvisors;


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

        public Builder requestAdvisor(RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest> advisor) {
            if (this.requestAdvisors == null) {
                this.requestAdvisors = new ArrayList<>();
            }
            this.requestAdvisors.add(advisor);
            return this;
        }

        public Builder requestAdvisors(List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> advisors) {
            this.requestAdvisors = advisors;
            return this;
        }

        public Builder chatResponseAdvisor(ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiChatCompletionResponse> advisor) {
            if (this.chatResponseAdvisors == null) {
                this.chatResponseAdvisors = new ArrayList<>();
            }
            this.chatResponseAdvisors.add(advisor);
            return this;
        }

        public Builder chatResponseAdvisors(List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiChatCompletionResponse>> advisors) {
            this.chatResponseAdvisors = advisors;
            return this;
        }

        public Builder streamResponseAdvisor(ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiStreamCompletionResponse> advisor) {
            if (this.streamResponseAdvisors == null) {
                this.streamResponseAdvisors = new ArrayList<>();
            }
            this.streamResponseAdvisors.add(advisor);
            return this;
        }

        public Builder streamResponseAdvisors(List<ResponseAdvisor<OpenAiChatCompletionRawResponse, OpenAiStreamCompletionResponse>> advisors) {
            this.streamResponseAdvisors = advisors;
            return this;
        }

        public OpenAiChatCompletionRequest build() {
            return new OpenAiChatCompletionRequest(this);
        }
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiChatCompletionRequest{" +
                "baseUrl='" + baseRequest.getBaseUrl() + '\'' +
                ", model='" + baseRequest.getModel() + '\'' +
                ", messageCount=" + chatRequest.getMessages().size() +
                ", hasCompletionOptions=" + (completionOptions != null) +
                ", requestAdvisorCount=" + requestAdvisors.size() +
                ", chatResponseAdvisorCount=" + chatResponseAdvisors.size() +
                ", streamResponseAdvisorCount=" + streamResponseAdvisors.size() +
                '}';
    }

}