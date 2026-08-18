package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * 对 OpenAI 流式请求应用 request advisors，并显式打开 stream。
 */
@Slf4j
public class OpenAiStreamEnhanceRequestStep implements StreamSyncStep {

    private final OpenAiAdvisorsExecutor clientSupport;

    public OpenAiStreamEnhanceRequestStep(OpenAiAdvisorsExecutor clientSupport) {
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
        rawRequest.setStreamOptions(Map.of("include_usage", true));

        log.debug(
                "Enhanced stream request. execId={}, iter={}, advisors={}, stream={}",
                context.getExecutionId(),
                context.getIteration(),
                providerRequest.getRequestAdvisors() == null ? 0 : providerRequest.getRequestAdvisors().size(),
                rawRequest.getStream()
        );

        return StreamStepKey.SEND_REQUEST;
    }
}