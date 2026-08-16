package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.Objects;

/**
 * 对 OpenAI 响应应用 response advisors。
 */
public class OpenAiChatEnhanceResponseStep implements ChatStep {

    private final OpenAiAdvisorsSupport clientSupport;

    public OpenAiChatEnhanceResponseStep(OpenAiAdvisorsSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);
        OpenAiChatCompletionRawResponse rawResponse = context.getAttribute(
                OpenAiAgentAttributes.RAW_RESPONSE,
                OpenAiChatCompletionRawResponse.class
        );
        OpenAiChatCompletionResponse providerResponse = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (rawResponse == null) {
            throw new IllegalStateException("Missing raw response in agent context");
        }
        if (providerResponse == null) {
            throw new IllegalStateException("Missing provider response in agent context");
        }

        clientSupport.applyChatResponseAdvisors(providerRequest, rawResponse, providerResponse);
        return ChatStepKey.ANALYZE_RESPONSE;
    }
}