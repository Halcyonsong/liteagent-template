package io.github.halcyonsong.liteagent.provider.openai.transport;

import io.github.halcyonsong.liteagent.core.exception.ModelException;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;

/**
 * OpenAI-compatible 协议发送器。
 * <p>
 * 仅负责发送 raw request 并接收 raw response，不参与上层模型转换。
 */
public class OpenAiTransport {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTransport.class);

    private final WebClient webClient;

    public OpenAiTransport(WebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient must not be null");
    }

    public OpenAiChatCompletionRawResponse send(String endpoint,
                                                String apiKey,
                                                OpenAiChatCompletionRawRequest rawRequest) {
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        Objects.requireNonNull(apiKey, "apiKey must not be null");
        Objects.requireNonNull(rawRequest, "rawRequest must not be null");

        try {
            log.debug("Sending OpenAI-compatible HTTP request. endpoint={}, model={}, stream={}, messageCount={}",
                    endpoint,
                    rawRequest.getModel(),
                    rawRequest.getStream(),
                    rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size());

            OpenAiChatCompletionRawResponse rawResponse = webClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(rawRequest)
                    .retrieve()
                    .bodyToMono(OpenAiChatCompletionRawResponse.class)
                    .block();

            if (rawResponse == null) {
                log.warn("Received null response from OpenAI-compatible API. endpoint={}, model={}",
                        endpoint, rawRequest.getModel());
                throw new ModelException("OpenAI response is null");
            }

            log.debug("Received OpenAI-compatible HTTP response. endpoint={}, responseId={}, model={}, choiceCount={}",
                    endpoint,
                    rawResponse.getId(),
                    rawResponse.getModel(),
                    rawResponse.getChoices() == null ? 0 : rawResponse.getChoices().size());

            return rawResponse;
        } catch (WebClientResponseException e) {
            log.error("OpenAI-compatible API error. endpoint={}, status={}, responseBody={}",
                    endpoint,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e);

            throw new ModelException(
                    "OpenAI-compatible API error: status=" + e.getStatusCode()
                            + ", body=" + e.getResponseBodyAsString(),
                    e
            );
        } catch (ModelException e) {
            log.error("Model exception occurred while calling OpenAI-compatible API. endpoint={}, message={}",
                    endpoint,
                    e.getMessage(),
                    e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to call OpenAI-compatible chat completion. endpoint={}, model={}",
                    endpoint,
                    rawRequest.getModel(),
                    e);
            throw new ModelException("Failed to call OpenAI-compatible chat completion", e);
        }
    }
}