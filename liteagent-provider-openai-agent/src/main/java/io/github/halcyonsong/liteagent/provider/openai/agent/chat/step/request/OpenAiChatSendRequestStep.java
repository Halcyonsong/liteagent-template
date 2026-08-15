package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
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
        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);
        OpenAiChatCompletionRawRequest rawRequest =
                OpenAiAgentRequestSupport.requireRawRequest(context);

        String endpoint = OpenAiAgentRequestSupport.resolveEndpoint(providerRequest);
        String apiKey = OpenAiAgentRequestSupport.resolveApiKey(providerRequest);

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        context.setAttribute(OpenAiAgentAttributes.RAW_RESPONSE, rawResponse);

        return ChatStepKey.MAP_RESPONSE;
    }
}