package io.github.halcyonsong.liteagent.provider.openai.client;

import io.github.halcyonsong.liteagent.core.client.StreamClient;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChunk;
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
 * 负责上层流式请求编排、raw request/raw response 转换以及统一流式结果输出。
 */
@Slf4j
public class OpenAiStreamClient implements StreamClient {

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
     * 使用统一调用对象发起流式对话请求，并返回框架统一流式结果。
     * <p>
     * 该方法面向希望使用 core 层统一流式抽象的调用方，
     * 返回 {@link io.github.halcyonsong.liteagent.core.model.response.stream.StreamChunk} 的响应流。
     *
     * @param invocation 统一聊天调用对象
     * @return 框架统一流式结果
     */
    @Override
    public Flux<StreamChunk> stream(ChatInvocation invocation) {
        log.debug("Stream invoked by ChatInvocation. messageCount={}, hasChatOptions={}",
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);
        return streamCompletion(invocation).map(OpenAiStreamCompletionResponse::toStreamChunk);
    }

    /**
     * 使用 provider 请求对象发起流式对话请求，并返回 provider 流式响应包装流。
     * <p>
     * 该方法适合需要保留 OpenAI-compatible 协议流式语义的调用方。
     *
     * @param request OpenAI-compatible provider 请求对象
     * @return provider 层流式响应包装流
     */
    public Flux<OpenAiStreamCompletionResponse> stream(OpenAiChatCompletionRequest request) {
        log.debug("Stream invoked by OpenAiChatCompletionRequest. model={}",
                request.getBaseRequest().getModel());
        return streamCompletion(request);
    }

    /**
     * 使用统一调用对象发起流式 completion 请求，并返回 provider 流式响应包装流。
     * <p>
     * 该方法会先将统一调用对象转换为 provider 请求对象，
     * 再执行流式 completion 请求。
     *
     * @param invocation 统一聊天调用对象
     * @return provider 层流式 completion 响应流
     */
    public Flux<OpenAiStreamCompletionResponse> streamCompletion(ChatInvocation invocation) {
        log.debug("Building OpenAI streaming request from ChatInvocation. model={}, messageCount={}, hasChatOptions={}",
                invocation.getBaseRequest().getModel(),
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(invocation.getChatRequest())
                .completionOptions(requestMapper.toCompletionOptions(invocation.getChatOptions()))
                .build();

        return streamCompletion(request);
    }

    /**
     * 使用 provider 请求对象执行流式 completion 请求。
     * <p>
     * 该方法负责：
     * <ul>
     *   <li>将 provider 请求映射为 raw request</li>
     *   <li>设置 stream=true</li>
     *   <li>规范化请求地址</li>
     *   <li>发送流式 HTTP 请求并映射响应</li>
     * </ul>
     *
     * @param request OpenAI-compatible provider 请求对象
     * @return provider 层流式 completion 响应流
     */
    public Flux<OpenAiStreamCompletionResponse> streamCompletion(OpenAiChatCompletionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(request);
        clientSupport.applyAdvisors(request, rawRequest);
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
                .map(responseMapper::fromRaw)
                .doOnNext(response -> log.debug(
                        "Mapped OpenAI-compatible streaming response. responseId={}, choiceCount={}, model={}",
                        response.getBaseResponse().getId(),
                        response.getChoices().size(),
                        response.getBaseResponse().getModel()
                ));
    }

    /**
     * 使用快速请求对象发起流式对话请求，并返回框架统一流式结果。
     * <p>
     * 适合快速测试和最小流式调用场景。
     *
     * @param request 快速聊天请求对象
     * @return 框架统一流式结果
     */
    public Flux<StreamChunk> stream(OpenAiQuickChatRequest request) {
        log.debug("Stream invoked by OpenAiQuickChatRequest. model={}, hasSystemMessage={}",
                request.getModel(),
                request.getSystemMessage() != null && !request.getSystemMessage().isBlank());
        return stream(request.toInvocation());
    }

    /**
     * 使用快速请求对象发起流式对话请求，并返回 provider 流式响应包装流。
     *
     * @param request 快速聊天请求对象
     * @return provider 层流式 completion 响应流
     */
    public Flux<OpenAiStreamCompletionResponse> streamCompletion(OpenAiQuickChatRequest request) {
        log.debug("StreamCompletion invoked by OpenAiQuickChatRequest. model={}, userMessageLength={}",
                request.getModel(),
                request.getUserMessage() == null ? 0 : request.getUserMessage().length());
        return streamCompletion(request.toInvocation());
    }

}