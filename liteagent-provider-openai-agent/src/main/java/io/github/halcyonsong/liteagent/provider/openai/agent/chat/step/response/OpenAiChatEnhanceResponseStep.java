package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
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
public class OpenAiChatEnhanceResponseStep implements ChatStep {

    private final OpenAiClientSupport clientSupport;

    public OpenAiChatEnhanceResponseStep(OpenAiClientSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        OpenAiChatCompletionRawResponse rawResponse = context.getAttribute(
                OpenAiChatAgentAttributes.RAW_RESPONSE,
                OpenAiChatCompletionRawResponse.class
        );
        OpenAiChatCompletionResponse providerResponse = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (providerRequest == null || rawResponse == null || providerResponse == null) {
            throw new IllegalStateException("Missing required context attributes for response enhancement");
        }

        clientSupport.applyChatResponseAdvisors(providerRequest, rawResponse, providerResponse);

        return ChatStepKey.ANALYZE_RESPONSE;
    }
}