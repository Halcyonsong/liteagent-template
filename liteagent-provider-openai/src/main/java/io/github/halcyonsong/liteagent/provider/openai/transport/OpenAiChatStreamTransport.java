package io.github.halcyonsong.liteagent.provider.openai.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.exception.ModelException;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiJsonSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * OpenAI-compatible 流式对话请求发送器。
 */
@Slf4j
public class OpenAiChatStreamTransport {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiChatStreamTransport(WebClient webClient) {
        this(webClient, OpenAiJsonSupport.getObjectMapper());
    }

    public OpenAiChatStreamTransport(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = Objects.requireNonNull(webClient, "webClient must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public Flux<OpenAiChatCompletionRawResponse> send(String endpoint,
                                                      String apiKey,
                                                      OpenAiChatCompletionRawRequest rawRequest) {
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        Objects.requireNonNull(apiKey, "apiKey must not be null");
        Objects.requireNonNull(rawRequest, "rawRequest must not be null");

        log.debug("Sending OpenAI-compatible streaming HTTP request. endpoint={}, model={}, stream={}, messageCount={}",
                endpoint,
                rawRequest.getModel(),
                rawRequest.getStream(),
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size());

        return webClient.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(rawRequest)
                .retrieve()
                .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(data -> !data.isEmpty())
                .filter(data -> !"[DONE]".equals(data))
                .map(this::deserializeChunk)
                .doOnNext(chunk -> log.debug(
                        "Received OpenAI-compatible streaming chunk. responseId={}, model={}, choiceCount={}",
                        chunk.getId(),
                        chunk.getModel(),
                        chunk.getChoices() == null ? 0 : chunk.getChoices().size()
                ))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("OpenAI-compatible streaming API error. endpoint={}, status={}, responseBody={}",
                            endpoint,
                            e.getStatusCode(),
                            e.getResponseBodyAsString(),
                            e);

                    return new ModelException(
                            "OpenAI-compatible streaming API error: status=" + e.getStatusCode()
                                    + ", body=" + e.getResponseBodyAsString(),
                            e
                    );
                })
                .onErrorMap(ModelException.class, e -> e)
                .onErrorMap(e -> {
                    log.error("Failed to call OpenAI-compatible streaming chat completion. endpoint={}, model={}",
                            endpoint,
                            rawRequest.getModel(),
                            e);
                    return new ModelException("Failed to call OpenAI-compatible streaming chat completion", e);
                });
    }

    private OpenAiChatCompletionRawResponse deserializeChunk(String json) {
        try {
            return objectMapper.readValue(json, OpenAiChatCompletionRawResponse.class);
        } catch (Exception e) {
            throw new ModelException("Failed to deserialize OpenAI-compatible streaming chunk: " + json, e);
        }
    }
}