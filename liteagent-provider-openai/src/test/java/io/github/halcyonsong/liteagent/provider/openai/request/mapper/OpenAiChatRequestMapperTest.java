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

    @Test
    void to_raw_request_should_map_base_request_and_chat_options() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("you are helpful"))
                .addMessage(Messages.user("hello"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .stream(false)
                .temperature(0.7)
                .maxTokens(256)
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        OpenAiChatRequestMapper mapper = new OpenAiChatRequestMapper();
        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals("test-model", rawRequest.getModel());
        assertEquals(false, rawRequest.getStream());
        assertEquals(0.7, rawRequest.getTemperature());
        assertEquals(256, rawRequest.getMaxTokens());

        assertEquals(2, rawRequest.getMessages().size());
        assertEquals("system", rawRequest.getMessages().get(0).get("role"));
        assertEquals("you are helpful", rawRequest.getMessages().get(0).get("content"));
        assertEquals("user", rawRequest.getMessages().get(1).get("role"));
        assertEquals("hello", rawRequest.getMessages().get(1).get("content"));
    }

    @Test
    void to_raw_request_should_map_completion_options() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("hello"))
                .build();

        OpenAiCompletionOptions completionOptions = OpenAiCompletionOptions.builder()
                .topP(0.95)
                .n(3)
                .stop(OpenAiCompletionOptions.Stop.of(List.of("END", "STOP")))
                .presencePenalty(1.1)
                .frequencyPenalty(0.9)
                .responseFormat(Map.of("type", "json_object"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .completionOptions(completionOptions)
                .build();

        OpenAiChatRequestMapper mapper = new OpenAiChatRequestMapper();
        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals(0.95, rawRequest.getTopP());
        assertEquals(3, rawRequest.getN());
        assertEquals(List.of("END", "STOP"), rawRequest.getStop());
        assertEquals(1.1, rawRequest.getPresencePenalty());
        assertEquals(0.9, rawRequest.getFrequencyPenalty());
        assertEquals("json_object", rawRequest.getResponseFormat().get("type"));
    }

    @Test
    void to_raw_request_should_allow_null_optional_fields() {
        OpenAiBaseRequest baseRequest = OpenAiBaseRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("hello"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(null)
                .completionOptions(null)
                .build();

        OpenAiChatRequestMapper mapper = new OpenAiChatRequestMapper();
        OpenAiChatCompletionRawRequest rawRequest = mapper.toRawRequest(request);

        assertEquals("test-model", rawRequest.getModel());
        assertEquals(1, rawRequest.getMessages().size());
        assertNull(rawRequest.getStream());
        assertNull(rawRequest.getTemperature());
        assertNull(rawRequest.getMaxTokens());
        assertNull(rawRequest.getTopP());
        assertNull(rawRequest.getN());
        assertNull(rawRequest.getStop());
        assertNull(rawRequest.getPresencePenalty());
        assertNull(rawRequest.getFrequencyPenalty());
        assertNull(rawRequest.getResponseFormat());
    }
}