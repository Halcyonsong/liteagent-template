package io.github.halcyonsong.example.support;

import io.github.halcyonsong.example.config.OpenAiProperties;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class OpenAiExampleSupport {

    @Autowired
    protected OpenAiProperties properties;

    protected OpenAiBaseRequest createBaseRequest() {
        return OpenAiBaseRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .build();
    }

    protected HttpRuntimeConfig buildRuntimeConfig() {
        return HttpRuntimeConfig.builder()
                .maxInMemorySize(properties.getRuntime().getMaxInMemorySize())
                .connectTimeoutMillis(properties.getRuntime().getConnectTimeoutMillis())
                .responseTimeoutMillis(properties.getRuntime().getResponseTimeoutMillis())
                .build();
    }

    protected HttpRuntimeConfig buildStreamRuntimeConfig() {
        return HttpRuntimeConfig.builder()
                .maxInMemorySize(properties.getRuntime().getMaxInMemorySize())
                .connectTimeoutMillis(properties.getRuntime().getConnectTimeoutMillis())
                .streamResponseTimeoutMillis(properties.getRuntime().getStreamResponseTimeoutMillis())
                .build();
    }

    protected void assumeConfigReady() {
        Assumptions.assumeTrue(Boolean.TRUE.equals(properties.getEnabled()), "openai example disabled");
        Assumptions.assumeTrue(properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank(), "baseUrl is blank");
        Assumptions.assumeTrue(properties.getApiKey() != null && !properties.getApiKey().isBlank(), "apiKey is blank");
        Assumptions.assumeTrue(properties.getModel() != null && !properties.getModel().isBlank(), "model is blank");
    }
}
