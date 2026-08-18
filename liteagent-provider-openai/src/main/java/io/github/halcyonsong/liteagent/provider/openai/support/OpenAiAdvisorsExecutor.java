package io.github.halcyonsong.liteagent.provider.openai.support;

import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI-compatible 增强器执行器。
 */
@Slf4j
public class OpenAiAdvisorsExecutor {

    public void applyRequestAdvisors(OpenAiChatCompletionRequest request,
                                     OpenAiChatCompletionRawRequest rawRequest) {
        if (request.getRequestAdvisors() == null || request.getRequestAdvisors().isEmpty()) {
            return;
        }
        log.debug("Applying request advisors. advisors={}, model={}",
                request.getRequestAdvisors().size(),
                request.getBaseRequest().getModel()
        );
        for (var advisor : request.getRequestAdvisors()) {
            advisor.enhance(request, rawRequest);
        }
    }

    public void applyChatResponseAdvisors(OpenAiChatCompletionRequest request,
                                          OpenAiChatCompletionRawResponse rawResponse,
                                          OpenAiChatCompletionResponse response) {
        if (request.getChatResponseAdvisors() == null || request.getChatResponseAdvisors().isEmpty()) {
            return;
        }
        log.debug("Applying chat response advisors. advisors={}, id={}",
                request.getChatResponseAdvisors().size(),
                rawResponse == null ? null : rawResponse.getId()
        );
        for (var advisor : request.getChatResponseAdvisors()) {
            advisor.enhance(rawResponse, response);
        }
    }

    public void applyStreamResponseAdvisors(OpenAiChatCompletionRequest request,
                                            OpenAiChatCompletionRawResponse rawResponse,
                                            OpenAiStreamCompletionResponse response) {
        if (request.getStreamResponseAdvisors() == null || request.getStreamResponseAdvisors().isEmpty()) {
            return;
        }
        log.debug("Applying stream response advisors. advisors={}, id={}",
                request.getStreamResponseAdvisors().size(),
                rawResponse == null ? null : rawResponse.getId()
        );
        for (var advisor : request.getStreamResponseAdvisors()) {
            advisor.enhance(rawResponse, response);
        }
    }

}
