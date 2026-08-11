package io.github.halcyonsong.liteagent.provider.openai.client;

import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * OpenAI-compatible 对话客户端。
 * <p>
 * 负责上层请求编排、raw request/raw response 转换以及统一结果输出。
 */
public class OpenAiChatClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiChatResponseMapper responseMapper;
    private final OpenAiTransport transport;

    public OpenAiChatClient(WebClient webClient) {
        this.requestMapper = new OpenAiChatRequestMapper();
        this.responseMapper = new OpenAiChatResponseMapper();
        this.transport = new OpenAiTransport(webClient);
    }

    public OpenAiChatClient(OpenAiChatRequestMapper requestMapper,
                            OpenAiChatResponseMapper responseMapper,
                            OpenAiTransport transport) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    public ChatResult chat(ChatInvocation invocation) {
        log.debug("Chat invoked by ChatInvocation. messageCount={}, hasChatOptions={}",
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);
        return chatCompletion(invocation).toChatResult();
    }

    public OpenAiChatCompletionResponse chat(OpenAiChatCompletionRequest request) {
        log.debug("Chat invoked by OpenAiChatCompletionRequest. model={}",
                request.getBaseRequest().getModel());
        return chatCompletion(request);
    }

    public OpenAiChatCompletionResponse chatCompletion(ChatInvocation invocation) {
        log.debug("Building OpenAI completion request from ChatInvocation. model={}, messageCount={}, hasChatOptions={}",
                invocation.getBaseRequest().getModel(),
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(invocation.getChatRequest())
                .chatOptions(invocation.getChatOptions())
                .build();

        return chatCompletion(request);
    }

    public OpenAiChatCompletionResponse chatCompletion(OpenAiChatCompletionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(request);
        String endpoint = normalizeChatCompletionsEndpoint(request.getBaseRequest().getBaseUrl());
        String apiKey = request.getBaseRequest().getApiKey();

        log.debug("Prepared OpenAI-compatible request. model={}, endpoint={}, messageCount={}, stream={}",
                rawRequest.getModel(),
                endpoint,
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size(),
                rawRequest.getStream());

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        OpenAiChatCompletionResponse response = responseMapper.fromRaw(rawResponse);

        log.debug("Mapped OpenAI-compatible response. responseId={}, choiceCount={}, model={}",
                response.getBaseResponse().getId(),
                response.getChoices().size(),
                response.getBaseResponse().getModel());

        return response;
    }

    private String normalizeChatCompletionsEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }

        String normalized = trimTrailingSlash(baseUrl.trim());

        if (normalized.endsWith("/v1/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1/chat")) {
            return normalized + "/completions";
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public ChatResult chat(OpenAiQuickChatRequest request) {
        log.debug("Chat invoked by OpenAiQuickChatRequest. model={}, hasSystemMessage={}",
                request.getModel(),
                request.getSystemMessage() != null && !request.getSystemMessage().isBlank());
        return chat(request.toInvocation());
    }

    public OpenAiChatCompletionResponse chatCompletion(OpenAiQuickChatRequest request) {
        log.debug("ChatCompletion invoked by OpenAiQuickChatRequest. model={}, userMessageLength={}",
                request.getModel(),
                request.getUserMessage() == null ? 0 : request.getUserMessage().length());
        return chatCompletion(request.toInvocation());
    }

}