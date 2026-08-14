package io.github.halcyonsong.liteagent.provider.openai.agent.step.response;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.Objects;

/**
 * 将 OpenAI raw response 映射为 provider response。
 * <p>
 * 该步骤后面可以自然插入响应增强器。
 */
public class OpenAiMapChatResponseStep implements AgentStep {

    private final OpenAiChatResponseMapper responseMapper;

    public OpenAiMapChatResponseStep(OpenAiChatResponseMapper responseMapper) {
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
    }

    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionRawResponse rawResponse = context.getAttribute(
                OpenAiAgentAttributes.RAW_RESPONSE,
                OpenAiChatCompletionRawResponse.class
        );

        if (rawResponse == null) {
            throw new IllegalStateException("Missing raw response in agent context");
        }

        OpenAiChatCompletionResponse providerResponse = responseMapper.fromRaw(rawResponse);
        context.setAttribute(OpenAiAgentAttributes.PROVIDER_RESPONSE, providerResponse);

        return AgentStepKey.ENHANCE_RESPONSE;
    }
}