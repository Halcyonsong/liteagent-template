package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.List;

public final class OpenAiAgentRequestBuildSupport {

    private OpenAiAgentRequestBuildSupport() {
    }

    public static RequestBuildResult buildRequest(
            Invocation invocation,
            List<Message> workingMessages,
            OpenAiChatRequestMapper requestMapper
    ) {
        if (workingMessages == null || workingMessages.isEmpty()) {
            throw new IllegalStateException("Working messages must not be empty when mapping request");
        }

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessages(workingMessages)
                .build();

        OpenAiChatCompletionRequest providerRequest;
        if (invocation instanceof OpenAiChatCompletionRequest openAiRequest) {
            providerRequest = OpenAiChatCompletionRequest.builder()
                    .baseRequest(openAiRequest.getBaseRequest())
                    .chatRequest(chatRequest)
                    .completionOptions(openAiRequest.getCompletionOptions())
                    .requestAdvisors(openAiRequest.getRequestAdvisors())
                    .chatResponseAdvisors(openAiRequest.getChatResponseAdvisors())
                    .streamResponseAdvisors(openAiRequest.getStreamResponseAdvisors())
                    .build();
        } else {
            providerRequest = OpenAiChatCompletionRequest.builder()
                    .baseRequest(invocation.getBaseRequest())
                    .chatRequest(chatRequest)
                    .completionOptions(null)
                    .build();
        }

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(providerRequest);
        return new RequestBuildResult(providerRequest, rawRequest);
    }

    public record RequestBuildResult(
            OpenAiChatCompletionRequest providerRequest,
            OpenAiChatCompletionRawRequest rawRequest
    ) {
    }
}
