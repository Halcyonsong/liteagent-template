package io.github.halcyonsong.liteagent.provider.openai.client.support;

import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

public class OpenAiClientSupport {

    public void applyAdvisors(OpenAiChatCompletionRequest request,
                               OpenAiChatCompletionRawRequest rawRequest) {
        if (request.getAdvisors() == null || request.getAdvisors().isEmpty()) {
            return;
        }

        for (var advisor : request.getAdvisors()) {
            advisor.enhance(request, rawRequest);
        }
    }

}
