package io.github.halcyonsong.liteagent.provider.openai.support;

import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

public class OpenAiAdvisorsSupport {

    /**
     * 应用请求增强器。
     */
    public void applyRequestAdvisors(OpenAiChatCompletionRequest request,
                                     OpenAiChatCompletionRawRequest rawRequest) {
        if (request.getRequestAdvisors() == null || request.getRequestAdvisors().isEmpty()) {
            return;
        }
        for (var advisor : request.getRequestAdvisors()) {
            advisor.enhance(request, rawRequest);
        }
    }

    /**
     * 应用普通对话响应增强器。
     */
    public void applyChatResponseAdvisors(OpenAiChatCompletionRequest request,
                                          OpenAiChatCompletionRawResponse rawResponse,
                                          OpenAiChatCompletionResponse response) {
        if (request.getChatResponseAdvisors() == null || request.getChatResponseAdvisors().isEmpty()) {
            return;
        }
        for (var advisor : request.getChatResponseAdvisors()) {
            advisor.enhance(rawResponse, response);
        }
    }

    /**
     * 应用流式响应增强器。
     */
    public void applyStreamResponseAdvisors(OpenAiChatCompletionRequest request,
                                            OpenAiChatCompletionRawResponse rawResponse,
                                            OpenAiStreamCompletionResponse response) {
        if (request.getStreamResponseAdvisors() == null || request.getStreamResponseAdvisors().isEmpty()) {
            return;
        }
        for (var advisor : request.getStreamResponseAdvisors()) {
            advisor.enhance(rawResponse, response);
        }
    }

}
