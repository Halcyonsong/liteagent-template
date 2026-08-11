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
    }

    @Test
    void build_should_keep_custom_values() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(8 * 1024 * 1024)
                .connectTimeoutMillis(3000)
                .responseTimeoutMillis(45000L)
                .build();

        assertEquals(8 * 1024 * 1024, config.getMaxInMemorySize());
        assertEquals(3000, config.getConnectTimeoutMillis());
        assertEquals(45000L, config.getResponseTimeoutMillis());
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
    void to_key_should_return_expected_runtime_key() {
        HttpRuntimeConfig config = HttpRuntimeConfig.builder()
                .maxInMemorySize(1024)
                .connectTimeoutMillis(2000)
                .responseTimeoutMillis(30000L)
                .build();

        HttpRuntimeKey key = config.toKey();

        assertEquals(1024, key.getMaxInMemorySize());
        assertEquals(2000, key.getConnectTimeoutMillis());
        assertEquals(30000L, key.getResponseTimeoutMillis());
    }
}