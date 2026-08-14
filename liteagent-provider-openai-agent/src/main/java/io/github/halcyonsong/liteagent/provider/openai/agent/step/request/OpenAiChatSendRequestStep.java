package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.Objects;

/**
 * 发送一轮普通 OpenAI chat 请求，并将 raw response 写入上下文。
 */
public class OpenAiChatSendRequestStep implements ChatStep {

    private final OpenAiChatTransport transport;

    public OpenAiChatSendRequestStep(OpenAiChatTransport transport) {
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiChatAgentAttributes.RAW_REQUEST,
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
        context.setAttribute(OpenAiChatAgentAttributes.RAW_RESPONSE, rawResponse);

        return ChatStepKey.MAP_RESPONSE;
    }
}