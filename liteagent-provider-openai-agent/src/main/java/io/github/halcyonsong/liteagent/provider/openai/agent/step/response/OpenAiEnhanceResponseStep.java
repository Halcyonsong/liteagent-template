package io.github.halcyonsong.liteagent.provider.openai.agent.step.response;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.Objects;

/**
 * 对映射后的响应应用增强器。
 * <p>
 * 与请求侧的 ENHANCE_REQUEST 对称，
 * 在 MAP_CHAT_RESPONSE 之后、ANALYZE_RESPONSE 之前执行。
 */
public class OpenAiEnhanceResponseStep implements AgentStep {

    private final OpenAiClientSupport clientSupport;

    public OpenAiEnhanceResponseStep(OpenAiClientSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        OpenAiChatCompletionRawResponse rawResponse = context.getAttribute(
                OpenAiAgentAttributes.RAW_RESPONSE,
                OpenAiChatCompletionRawResponse.class
        );
        OpenAiChatCompletionResponse providerResponse = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (providerRequest == null || rawResponse == null || providerResponse == null) {
            throw new IllegalStateException("Missing required context attributes for response enhancement");
        }

        clientSupport.applyChatResponseAdvisors(providerRequest, rawResponse, providerResponse);

        return AgentStepKey.ANALYZE_RESPONSE;
    }
}