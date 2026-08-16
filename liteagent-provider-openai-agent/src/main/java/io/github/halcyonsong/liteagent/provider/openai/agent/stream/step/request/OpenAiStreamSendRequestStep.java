package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiStreamTransport;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 发起 OpenAI-compatible 流式请求，并将 raw chunk 映射为 provider 输出对象。
 */
public class OpenAiStreamSendRequestStep implements StreamStep<Flux<OpenAiStreamCompletionResponse>> {

    private final OpenAiStreamTransport transport;
    private final OpenAiStreamResponseMapper responseMapper;

    public OpenAiStreamSendRequestStep(OpenAiStreamTransport transport,
                                       OpenAiStreamResponseMapper responseMapper) {
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
    }

    @Override
    public StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> apply(
            Flux<OpenAiStreamCompletionResponse> upstream,
            StreamAgentContext<?> context
    ) {
        if (upstream != null) {
            throw new IllegalStateException("SEND_REQUEST must be the first stream step");
        }

        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);
        OpenAiChatCompletionRawRequest rawRequest =
                OpenAiAgentRequestSupport.requireRawRequest(context);

        String endpoint = OpenAiAgentRequestSupport.resolveEndpoint(providerRequest);
        String apiKey = OpenAiAgentRequestSupport.resolveApiKey(providerRequest);

        Flux<OpenAiStreamCompletionResponse> stream = transport.send(endpoint, apiKey, rawRequest)
                .map(responseMapper::fromRaw)
                .switchIfEmpty(Flux.error(new IllegalStateException("OpenAI stream response is empty")));


        return new StreamApplyResult<>(stream, StreamStepKey.ENHANCE_CHUNK);
    }
}