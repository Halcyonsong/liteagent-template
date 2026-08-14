package io.github.halcyonsong.liteagent.provider.openai.agent.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.Objects;

/**
 * 将 OpenAI raw response 映射为 provider response。
 * <p>
 * 该步骤后面可以自然插入响应增强器。
 */
public class OpenAiChatMapResponseStep implements ChatStep {

    private final OpenAiChatResponseMapper responseMapper;

    public OpenAiChatMapResponseStep(OpenAiChatResponseMapper responseMapper) {
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionRawResponse rawResponse = context.getAttribute(
                OpenAiChatAgentAttributes.RAW_RESPONSE,
                OpenAiChatCompletionRawResponse.class
        );

        if (rawResponse == null) {
            throw new IllegalStateException("Missing raw response in agent context");
        }

        OpenAiChatCompletionResponse providerResponse = responseMapper.fromRaw(rawResponse);
        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_RESPONSE, providerResponse);

        return ChatStepKey.ENHANCE_RESPONSE;
    }
}