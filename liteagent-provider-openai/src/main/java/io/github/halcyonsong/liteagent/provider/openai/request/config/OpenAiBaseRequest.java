package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.BaseRequest;

import java.util.Objects;

public class OpenAiBaseRequest implements BaseRequest {

    private final String baseUrl;
    private final String apiKey;
    private final String model;

    private OpenAiBaseRequest(Builder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey must not be null");
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl;
        private String apiKey;
        private String model;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public OpenAiBaseRequest build() {
            return new OpenAiBaseRequest(this);
        }
    }
}