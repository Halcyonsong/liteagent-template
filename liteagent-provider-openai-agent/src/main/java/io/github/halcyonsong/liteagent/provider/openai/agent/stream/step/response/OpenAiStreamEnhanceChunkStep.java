package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.constant.OpenAiStreamAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 对每个 stream chunk 应用 OpenAI response advisors。
 * <p>
 * 当前先只把 mapped chunk 透传给 advisor，
 * 后续如需同时透传 raw chunk，可再引入 exchange 包装对象。
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
                context.getAttribute(OpenAiStreamAgentAttributes.PROVIDER_REQUEST, OpenAiChatCompletionRequest.class);

        if (providerRequest == null) {
            throw new IllegalStateException("Missing provider request in stream context");
        }

        Flux<OpenAiStreamCompletionResponse> stream = upstream.doOnNext(chunk -> {
            // 当前先不传 rawResponse；后续如需 raw + mapped 双输入，再换成 exchange 结构
            clientSupport.applyStreamResponseAdvisors(providerRequest, null, chunk);
        });

        return new StreamApplyResult<>(stream, StreamStepKey.ACCUMULATE_CHUNK);
    }
}