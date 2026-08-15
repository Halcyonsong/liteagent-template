package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 对 OpenAI 流式 chunk 应用 response advisors。
 */
public class OpenAiStreamEnhanceChunkStep implements StreamStep<Flux<OpenAiStreamCompletionResponse>> {

    private final OpenAiClientSupport clientSupport;

    public OpenAiStreamEnhanceChunkStep(OpenAiClientSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> apply(
            Flux<OpenAiStreamCompletionResponse> upstream,
            StreamAgentContext<?> context
    ) {
        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);

        Flux<OpenAiStreamCompletionResponse> stream = upstream.doOnNext(
                response -> clientSupport.applyStreamResponseAdvisors(providerRequest, null, response)
        );

        return new StreamApplyResult<>(stream, StreamStepKey.ACCUMULATE_CHUNK);
    }
}