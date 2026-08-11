package io.github.halcyonsong.liteagent.core.model.request;

import io.github.halcyonsong.liteagent.core.message.type.UserMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatInvocationTest {

    @Test
    void build_should_fail_when_base_request_is_null() {
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(new UserMessage("hello"))
                .build();

        assertThrows(NullPointerException.class, () ->
                ChatInvocation.builder()
                        .chatRequest(chatRequest)
                        .build());
    }

    @Test
    void build_should_fail_when_chat_request_is_null() {
        assertThrows(NullPointerException.class, () ->
                ChatInvocation.builder()
                        .baseRequest(new TestBaseRequest("https://example.com", "test-key", "test-model"))
                        .build());
    }

    @Test
    void build_should_keep_all_fields() {
        BaseRequest baseRequest = new TestBaseRequest(
                "https://example.com",
                "test-key",
                "test-model"
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(new UserMessage("hello"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .stream(false)
                .temperature(0.7)
                .maxTokens(128)
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        assertSame(baseRequest, invocation.getBaseRequest());
        assertSame(chatRequest, invocation.getChatRequest());
        assertSame(chatOptions, invocation.getChatOptions());
    }

    @Test
    void build_should_allow_null_chat_options() {
        BaseRequest baseRequest = new TestBaseRequest(
                "https://example.com",
                "test-key",
                "test-model"
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(new UserMessage("hello"))
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(null)
                .build();

        assertNull(invocation.getChatOptions());
    }

    private static class TestBaseRequest implements BaseRequest {

        private final String baseUrl;
        private final String apiKey;
        private final String model;

        private TestBaseRequest(String baseUrl, String apiKey, String model) {
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.model = model;
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        @Override
        public String getModel() {
            return model;
        }
    }
}