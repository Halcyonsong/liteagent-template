package io.github.halcyonsong.liteagent.provider.openai.request.quickrequest;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiQuickChatRequestTest {

    @Test
    void to_base_request_should_build_expected_object() {
        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .userMessage("hello")
                .systemMessage("you are helpful")
                .build();

        OpenAiBaseRequest baseRequest = request.toBaseRequest();

        assertEquals("https://example.com", baseRequest.getBaseUrl());
        assertEquals("test-key", baseRequest.getApiKey());
        assertEquals("test-model", baseRequest.getModel());
    }

    @Test
    void to_chat_request_should_include_system_and_user_messages() {
        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .userMessage("hello")
                .systemMessage("you are helpful")
                .build();

        ChatRequest chatRequest = request.toChatRequest();

        assertEquals(2, chatRequest.getMessages().size());
        assertEquals(MessageRole.SYSTEM, chatRequest.getMessages().get(0).getRole());
        assertEquals("you are helpful", chatRequest.getMessages().get(0).getContent());
        assertEquals(MessageRole.USER, chatRequest.getMessages().get(1).getRole());
        assertEquals("hello", chatRequest.getMessages().get(1).getContent());
    }

    @Test
    void to_chat_request_should_skip_blank_system_message() {
        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .userMessage("hello")
                .systemMessage("   ")
                .build();

        ChatRequest chatRequest = request.toChatRequest();

        assertEquals(1, chatRequest.getMessages().size());
        assertEquals(MessageRole.USER, chatRequest.getMessages().get(0).getRole());
        assertEquals("hello", chatRequest.getMessages().get(0).getContent());
    }

    @Test
    void to_invocation_should_build_expected_object() {
        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl("https://example.com")
                .apiKey("test-key")
                .model("test-model")
                .userMessage("hello")
                .systemMessage("you are helpful")
                .build();

        ChatInvocation invocation = request.toInvocation();

        assertNotNull(invocation.getBaseRequest());
        assertNotNull(invocation.getChatRequest());
        assertNull(invocation.getChatOptions());
        assertEquals("https://example.com", invocation.getBaseRequest().getBaseUrl());
        assertEquals("test-model", invocation.getBaseRequest().getModel());
        assertEquals(2, invocation.getChatRequest().getMessages().size());
    }

    @Test
    void build_should_fail_when_required_fields_missing() {
        assertThrows(NullPointerException.class, () ->
                OpenAiQuickChatRequest.builder()
                        .apiKey("test-key")
                        .model("test-model")
                        .userMessage("hello")
                        .build());

        assertThrows(NullPointerException.class, () ->
                OpenAiQuickChatRequest.builder()
                        .baseUrl("https://example.com")
                        .model("test-model")
                        .userMessage("hello")
                        .build());

        assertThrows(NullPointerException.class, () ->
                OpenAiQuickChatRequest.builder()
                        .baseUrl("https://example.com")
                        .apiKey("test-key")
                        .userMessage("hello")
                        .build());

        assertThrows(NullPointerException.class, () ->
                OpenAiQuickChatRequest.builder()
                        .baseUrl("https://example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build());
    }
}