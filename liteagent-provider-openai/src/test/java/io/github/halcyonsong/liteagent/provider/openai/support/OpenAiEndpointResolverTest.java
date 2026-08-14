package io.github.halcyonsong.liteagent.provider.openai.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiEndpointResolverTest {

    @Test
    void resolve_should_keep_full_endpoint_unchanged() {
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.example.com/v1/chat/completions"
        );

        assertEquals("https://api.example.com/v1/chat/completions", endpoint);
    }

    @Test
    void resolve_should_complete_when_base_url_ends_with_v1_chat() {
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.example.com/v1/chat"
        );

        assertEquals("https://api.example.com/v1/chat/completions", endpoint);
    }

    @Test
    void resolve_should_complete_when_base_url_ends_with_v1() {
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.example.com/v1"
        );

        assertEquals("https://api.example.com/v1/chat/completions", endpoint);
    }

    @Test
    void resolve_should_complete_when_base_url_has_no_suffix() {
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.example.com"
        );

        assertEquals("https://api.example.com/v1/chat/completions", endpoint);
    }

    @Test
    void resolve_should_trim_trailing_slashes() {
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                "https://api.example.com///"
        );

        assertEquals("https://api.example.com/v1/chat/completions", endpoint);
    }

    @Test
    void resolve_should_reject_blank_base_url() {
        assertThrows(IllegalArgumentException.class,
                () -> OpenAiEndpointResolver.resolveChatCompletionsEndpoint(null));
        assertThrows(IllegalArgumentException.class,
                () -> OpenAiEndpointResolver.resolveChatCompletionsEndpoint(" "));
    }
}
