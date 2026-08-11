package io.github.halcyonsong.liteagent.provider.openai.request.mapper;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiChatRequestMapperTest {

    private final OpenAiChatRequestMapper mapper = new OpenAiChatRequestMapper();

    @Test
    void shouldMapBasicRequestSuccessfully() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://api.openai.com")
                .apiKey("test-key")
                .model("gpt-test")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一个助手"))
                .addMessage(Messages.user("你好"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build();

        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals("gpt-test", rawRequest.getModel());
        assertEquals(2, rawRequest.getMessages().size());

        Map<String, Object> systemMessage = rawRequest.getMessages().get(0);
        assertEquals("system", systemMessage.get("role"));
        assertEquals("你是一个助手", systemMessage.get("content"));

        Map<String, Object> userMessage = rawRequest.getMessages().get(1);
        assertEquals("user", userMessage.get("role"));
        assertEquals("你好", userMessage.get("content"));

        assertNull(rawRequest.getStream());
    }

    @Test
    void shouldMapChatOptionsSuccessfully() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://api.openai.com")
                .apiKey("test-key")
                .model("gpt-test")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(256)
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals(0.7, rawRequest.getTemperature());
        assertEquals(256, rawRequest.getMaxTokens());
        assertNull(rawRequest.getStream());
    }

    @Test
    void shouldMapCompletionOptionsSuccessfully() {
        OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
                .topP(0.8)
                .n(2)
                .presencePenalty(0.5)
                .frequencyPenalty(0.2)
                .responseFormat(Map.of("type", "json_object"))
                .stop(OpenAiCompletionOptions.Stop.of(List.of("END", "STOP")))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("test-key")
                        .model("gpt-test")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("你好"))
                        .build())
                .completionOptions(completionOptions)
                .build();

        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals(0.8, rawRequest.getTopP());
        assertEquals(2, rawRequest.getN());
        assertEquals(0.5, rawRequest.getPresencePenalty());
        assertEquals(0.2, rawRequest.getFrequencyPenalty());
        assertEquals("json_object", ((Map<?, ?>) rawRequest.getResponseFormat()).get("type"));
        assertEquals(List.of("END", "STOP"), rawRequest.getStop());
    }

    @Test
    void shouldMapSingleStopSuccessfully() {
        OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
                .stop(OpenAiCompletionOptions.Stop.of("END"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("test-key")
                        .model("gpt-test")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("你好"))
                        .build())
                .completionOptions(completionOptions)
                .build();

        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals("END", rawRequest.getStop());
    }
}