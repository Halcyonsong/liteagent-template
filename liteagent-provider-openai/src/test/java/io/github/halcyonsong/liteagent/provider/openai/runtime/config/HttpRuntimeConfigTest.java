package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpRuntimeConfigTest {

    @Test
    void build_should_use_default_values_when_fields_not_set() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder().build();

        assertEquals(16 * 1024 * 1024, config.getMaxInMemorySize());
        assertEquals(5000, config.getConnectTimeoutMillis());
        assertEquals(60000L, config.getResponseTimeoutMillis());
        assertNull(config.getStreamResponseTimeoutMillis());
    }

    @Test
    void build_should_keep_custom_values() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(8 * 1024 * 1024)
                .connectTimeoutMillis(3000)
                .responseTimeoutMillis(45000L)
                .streamResponseTimeoutMillis(300000L)
                .build();

        assertEquals(8 * 1024 * 1024, config.getMaxInMemorySize());
        assertEquals(3000, config.getConnectTimeoutMillis());
        assertEquals(45000L, config.getResponseTimeoutMillis());
        assertEquals(300000L, config.getStreamResponseTimeoutMillis());
    }

    @Test
    void build_should_fail_when_max_in_memory_size_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .maxInMemorySize(0)
                        .build());

        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .maxInMemorySize(-1)
                        .build());
    }

    @Test
    void build_should_fail_when_connect_timeout_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .connectTimeoutMillis(0)
                        .build());

        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .connectTimeoutMillis(-1)
                        .build());
    }

    @Test
    void build_should_fail_when_response_timeout_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .responseTimeoutMillis(0L)
                        .build());

        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .responseTimeoutMillis(-1L)
                        .build());
    }

    @Test
    void build_should_fail_when_stream_response_timeout_invalid() {
        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .streamResponseTimeoutMillis(0L)
                        .build());

        assertThrows(IllegalArgumentException.class, () ->
                HttpRuntimeConfig.builder()
                        .streamResponseTimeoutMillis(-1L)
                        .build());
    }

    @Test
    void to_key_should_return_expected_chat_runtime_key() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(1024)
                .connectTimeoutMillis(2000)
                .responseTimeoutMillis(30000L)
                .streamResponseTimeoutMillis(180000L)
                .build();

        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.CHAT);

        assertEquals(1024, key.getMaxInMemorySize());
        assertEquals(2000, key.getConnectTimeoutMillis());
        assertEquals(30000L, key.getResponseTimeoutMillis());
        assertEquals(180000L, key.getStreamResponseTimeoutMillis());
        assertEquals(HttpRuntimeMode.CHAT, key.getMode());
    }

    @Test
    void to_key_should_return_expected_stream_runtime_key() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(2048)
                .connectTimeoutMillis(2500)
                .responseTimeoutMillis(45000L)
                .streamResponseTimeoutMillis(240000L)
                .build();

        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.STREAM);

        assertEquals(2048, key.getMaxInMemorySize());
        assertEquals(2500, key.getConnectTimeoutMillis());
        assertEquals(45000L, key.getResponseTimeoutMillis());
        assertEquals(240000L, key.getStreamResponseTimeoutMillis());
        assertEquals(HttpRuntimeMode.STREAM, key.getMode());
    }

    @Test
    void to_key_should_distinguish_chat_and_stream_modes() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(1024)
                .connectTimeoutMillis(3000)
                .responseTimeoutMillis(60000L)
                .streamResponseTimeoutMillis(300000L)
                .build();

        HttpRuntimeKey chatKey = config.toKey(HttpRuntimeMode.CHAT);
        HttpRuntimeKey streamKey = config.toKey(HttpRuntimeMode.STREAM);

        assertNotEquals(chatKey, streamKey);
        assertEquals(HttpRuntimeMode.CHAT, chatKey.getMode());
        assertEquals(HttpRuntimeMode.STREAM, streamKey.getMode());
    }

    @Test
    void to_stream_key_should_allow_null_stream_response_timeout() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(4096)
                .connectTimeoutMillis(4000)
                .responseTimeoutMillis(60000L)
                .build();

        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.STREAM);

        assertEquals(4096, key.getMaxInMemorySize());
        assertEquals(4000, key.getConnectTimeoutMillis());
        assertEquals(60000L, key.getResponseTimeoutMillis());
        assertNull(key.getStreamResponseTimeoutMillis());
        assertEquals(HttpRuntimeMode.STREAM, key.getMode());
    }

    @Test
    void to_chat_key_should_match_shortcut_method() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(1024)
                .connectTimeoutMillis(3000)
                .responseTimeoutMillis(60000L)
                .streamResponseTimeoutMillis(120000L)
                .build();

        assertEquals(config.toKey(HttpRuntimeMode.CHAT), config.toChatKey());
    }

    @Test
    void to_stream_key_should_match_shortcut_method() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(1024)
                .connectTimeoutMillis(3000)
                .responseTimeoutMillis(60000L)
                .streamResponseTimeoutMillis(120000L)
                .build();

        assertEquals(config.toKey(HttpRuntimeMode.STREAM), config.toStreamKey());
    }
}