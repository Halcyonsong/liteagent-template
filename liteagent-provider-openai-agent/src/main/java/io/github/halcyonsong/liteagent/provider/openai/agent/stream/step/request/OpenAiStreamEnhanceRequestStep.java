package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.Objects;

/**
 * 对 OpenAI 流式请求应用 request advisors，并显式打开 stream。
 */
public class OpenAiStreamEnhanceRequestStep implements StreamSyncStep {

    private final OpenAiClientSupport clientSupport;

    public OpenAiStreamEnhanceRequestStep(OpenAiClientSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);
        OpenAiChatCompletionRawRequest rawRequest =
                OpenAiAgentRequestSupport.requireRawRequest(context);

        clientSupport.applyRequestAdvisors(providerRequest, rawRequest);
        rawRequest.setStream(true);

        return StreamStepKey.SEND_REQUEST;
    }
}