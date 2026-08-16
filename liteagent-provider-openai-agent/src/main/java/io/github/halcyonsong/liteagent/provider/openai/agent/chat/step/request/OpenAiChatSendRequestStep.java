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
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 发送一轮普通 OpenAI chat 请求，并将 raw response 写入上下文。
 */
@Slf4j
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
        log.debug(
                "Sending chat round. executionId={}, iteration={}, endpoint={}, messageCount={}",
                context.getExecutionId(),
                context.getIteration(),
                endpoint,
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size()
        );

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        context.setAttribute(OpenAiAgentAttributes.RAW_RESPONSE, rawResponse);
        log.debug(
                "Received raw chat response. executionId={}, iteration={}, responseId={}",
                context.getExecutionId(),
                context.getIteration(),
                rawResponse.getId()
        );

        return ChatStepKey.MAP_RESPONSE;
    }
}