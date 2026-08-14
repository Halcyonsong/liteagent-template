package io.github.halcyonsong.liteagent.provider.openai.client;

import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatStreamTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * OpenAI-compatible 流式对话客户端。
 * <p>
 * 负责 provider 流式请求编排、raw request/raw response 转换以及统一流式结果输出。
 */
@Slf4j
public class OpenAiStreamClient {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiStreamResponseMapper responseMapper;
    private final OpenAiChatStreamTransport transport;
    private final OpenAiClientSupport clientSupport = new OpenAiClientSupport();

    public OpenAiStreamClient(WebClient webClient) {
        this.requestMapper = new OpenAiChatRequestMapper();
        this.responseMapper = new OpenAiStreamResponseMapper();
        this.transport = new OpenAiChatStreamTransport(webClient);
    }

    public OpenAiStreamClient(OpenAiChatRequestMapper requestMapper,
                              OpenAiStreamResponseMapper responseMapper,
                              OpenAiChatStreamTransport transport) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    /**
     * 使用 provider 请求对象发起流式对话请求，并返回 provider 流式响应包装流。
     */
    public Flux<OpenAiStreamCompletionResponse> stream(OpenAiChatCompletionRequest request) {
        log.debug("Stream invoked by OpenAiChatCompletionRequest. model={}",
                request.getBaseRequest().getModel());
        return streamCompletion(request);
    }

    /**
     * 使用统一调用对象发起流式 completion 请求，并返回 provider 流式响应包装流。
     */
    public Flux<OpenAiStreamCompletionResponse> streamCompletion(Invocation invocation) {
        Objects.requireNonNull(invocation, "invocation must not be null");

        log.debug("Building OpenAI streaming request from Invocation. model={}, messageCount={}",
                invocation.getBaseRequest().getModel(),
                invocation.getChatRequest().getMessages().size());

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(invocation.getChatRequest())
                .completionOptions(null)
                .build();

        return streamCompletion(request);
    }

    /**
     * 使用 provider 请求对象执行流式 completion 请求。
     */
    public Flux<OpenAiStreamCompletionResponse> streamCompletion(OpenAiChatCompletionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(request);
        clientSupport.applyRequestAdvisors(request, rawRequest);
        rawRequest.setStream(true);

        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                request.getBaseRequest().getBaseUrl()
        );
        String apiKey = request.getBaseRequest().getApiKey();

        log.debug("Prepared OpenAI-compatible streaming request. model={}, endpoint={}, messageCount={}, stream={}",
                rawRequest.getModel(),
                endpoint,
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size(),
                rawRequest.getStream());

        return transport.send(endpoint, apiKey, rawRequest)
                .map(rawResponse -> {
                    OpenAiStreamCompletionResponse streamResponse = responseMapper.fromRaw(rawResponse);
                    clientSupport.applyStreamResponseAdvisors(request, rawResponse, streamResponse);
                    return streamResponse;
                })
                .doOnNext(response -> log.debug(
                        "Mapped OpenAI-compatible streaming response. responseId={}, choiceCount={}, model={}",
                        response.getBaseResponse().getId(),
                        response.getChoices().size(),
                        response.getBaseResponse().getModel()
                ));
    }

    /**
     * 使用快速请求对象发起流式对话请求，并返回 provider 流式响应包装流。
     */
    public Flux<OpenAiStreamCompletionResponse> stream(OpenAiQuickChatRequest request) {
        log.debug("StreamCompletion invoked by OpenAiQuickChatRequest. model={}, userMessageLength={}",
                request.getModel(),
                request.getUserMessage() == null ? 0 : request.getUserMessage().length());
        return streamCompletion(request.toRequest());
    }
}