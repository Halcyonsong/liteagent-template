package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 对 OpenAI 响应应用 response advisors。
 */
@Slf4j
public class OpenAiChatEnhanceResponseStep implements ChatStep {

    private final OpenAiAdvisorsExecutor clientSupport;

    public OpenAiChatEnhanceResponseStep(OpenAiAdvisorsExecutor clientSupport) {
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

        log.debug(
                "Enhanced OpenAI chat response. " +
                        "executionId={}, " +
                        "iteration={}, " +
                        "responseAdvisorCount={}",
                context.getExecutionId(),
                context.getIteration(),
                providerRequest.getChatResponseAdvisors().size()
        );

        return ChatStepKey.ANALYZE_RESPONSE;
    }
}