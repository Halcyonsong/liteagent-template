package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.Objects;

/**
 * 发送一轮普通 OpenAI chat 请求，并将 raw response 写入上下文。
 */
public class OpenAiSendChatRequestStep implements AgentStep {

    private final OpenAiChatTransport transport;

    public OpenAiSendChatRequestStep(OpenAiChatTransport transport) {
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );

        if (providerRequest == null) {
            throw new IllegalStateException("Missing provider request in agent context");
        }
        if (rawRequest == null) {
            throw new IllegalStateException("Missing raw request in agent context");
        }

        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                providerRequest.getBaseRequest().getBaseUrl()
        );
        String apiKey = providerRequest.getBaseRequest().getApiKey();

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        context.setAttribute(OpenAiAgentAttributes.RAW_RESPONSE, rawResponse);

        return AgentStepKey.MAP_CHAT_RESPONSE;
    }
}