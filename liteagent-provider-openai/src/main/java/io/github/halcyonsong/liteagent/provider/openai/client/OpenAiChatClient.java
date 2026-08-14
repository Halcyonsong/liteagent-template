package io.github.halcyonsong.liteagent.provider.openai.client;

import io.github.halcyonsong.liteagent.core.client.ChatClient;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * OpenAI-compatible 对话客户端。
 * <p>
 * 负责 provider 请求编排、raw request/raw response 转换以及结果输出。
 */
@Slf4j
public class OpenAiChatClient implements ChatClient {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiChatResponseMapper responseMapper;
    private final OpenAiChatTransport transport;
    private final OpenAiClientSupport clientSupport = new OpenAiClientSupport();

    public OpenAiChatClient(WebClient webClient) {
        this.requestMapper = new OpenAiChatRequestMapper();
        this.responseMapper = new OpenAiChatResponseMapper();
        this.transport = new OpenAiChatTransport(webClient);
    }

    public OpenAiChatClient(OpenAiChatRequestMapper requestMapper,
                            OpenAiChatResponseMapper responseMapper,
                            OpenAiChatTransport transport) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    /**
     * 使用统一调用对象发起普通对话请求，并返回统一结果抽象。
     * 当前 OpenAI provider 返回具体的 provider 响应对象。
     */
    @Override
    public Result chat(Invocation invocation) {
        return chatCompletion(invocation);
    }

    /**
     * 使用 provider 请求对象发起普通对话请求，并返回 provider 响应包装对象。
     */
    public OpenAiChatCompletionResponse chat(OpenAiChatCompletionRequest request) {
        log.debug("Chat invoked by OpenAiChatCompletionRequest. model={}",
                request.getBaseRequest().getModel());
        return chatCompletion(request);
    }

    /**
     * 使用统一调用对象发起普通对话请求，并返回 provider 响应包装对象。
     */
    public OpenAiChatCompletionResponse chatCompletion(Invocation invocation) {
        Objects.requireNonNull(invocation, "invocation must not be null");

        log.debug("Building OpenAI completion request from Invocation. model={}, messageCount={}",
                invocation.getBaseRequest().getModel(),
                invocation.getChatRequest().getMessages().size());

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(invocation.getChatRequest())
                .completionOptions(null)
                .build();

        return chatCompletion(request);
    }

    /**
     * 使用 provider 请求对象执行普通 completion 请求。
     */
    public OpenAiChatCompletionResponse chatCompletion(OpenAiChatCompletionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(request);
        clientSupport.applyRequestAdvisors(request, rawRequest);
        rawRequest.setStream(false);

        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                request.getBaseRequest().getBaseUrl()
        );
        String apiKey = request.getBaseRequest().getApiKey();

        log.debug("Prepared OpenAI-compatible request. model={}, endpoint={}, messageCount={}, stream={}",
                rawRequest.getModel(),
                endpoint,
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size(),
                rawRequest.getStream());

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        OpenAiChatCompletionResponse response = responseMapper.fromRaw(rawResponse);
        clientSupport.applyChatResponseAdvisors(request, rawResponse, response);

        log.debug("Mapped OpenAI-compatible response. responseId={}, choiceCount={}, model={}",
                response.getBaseResponse().getId(),
                response.getChoices().size(),
                response.getBaseResponse().getModel());

        return response;
    }

    /**
     * 使用快速请求对象发起普通对话请求，并返回统一结果抽象。
     */
    public Result chat(OpenAiQuickChatRequest request) {
        log.debug("Chat invoked by OpenAiQuickChatRequest. model={}, hasSystemMessage={}",
                request.getModel(),
                request.getSystemMessage() != null && !request.getSystemMessage().isBlank());
        return chat(request.toRequest());
    }

    /**
     * 使用快速请求对象发起普通对话请求，并返回 provider 响应包装对象。
     */
    public OpenAiChatCompletionResponse chatCompletion(OpenAiQuickChatRequest request) {
        log.debug("ChatCompletion invoked by OpenAiQuickChatRequest. model={}, userMessageLength={}",
                request.getModel(),
                request.getUserMessage() == null ? 0 : request.getUserMessage().length());
        return chatCompletion(request.toRequest());
    }
}