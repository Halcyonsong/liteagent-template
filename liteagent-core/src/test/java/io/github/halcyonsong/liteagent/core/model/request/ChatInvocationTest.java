package io.github.halcyonsong.liteagent.core.model.request;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatInvocationTest {

    @Test
    void shouldBuildChatInvocationSuccessfully() {
        BaseRequest baseRequest = new TestBaseRequest("https://api.test.com", "test-key", "test-model");
        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("hello"))
                .build();
        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.6)
                .maxTokens(256)
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        assertEquals(baseRequest, invocation.getBaseRequest());
        assertEquals(chatRequest, invocation.getChatRequest());
        assertEquals(chatOptions, invocation.getChatOptions());
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