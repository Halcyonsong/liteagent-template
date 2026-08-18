package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

import lombok.Getter;

/**
 * 基础 WebClient 运行时配置。控制 HTTP 客户端网络传输和响应解码行为，
 * 不含 provider 地址、鉴权或单次请求参数。
 */
@Getter
public class HttpRuntimeConfig {

    /** 单次响应体最大聚合内存字节数，默认 16MB。 */
    private final Integer maxInMemorySize;

    /** 连接超时时间（毫秒），默认 5000。 */
    private final Integer connectTimeoutMillis;

    /** 响应超时时间（毫秒），默认 60000。 */
    private final Long chatResponseTimeoutMillis;

    /** 流式响应超时时间（毫秒），默认不设置超时。 */
    private final Long streamResponseTimeoutMillis;

    /** 连接池最大连接数，默认 50。 */
    private final Integer maxConnections;

    /** 连接最大空闲时间（毫秒），默认 30000。 */
    private final Long maxIdleTimeMillis;

    /** 连接最大生命周期（毫秒），默认 300000。 */
    private final Long maxLifeTimeMillis;

    private HttpRuntimeConfig(Builder builder) {
        this.maxInMemorySize = builder.maxInMemorySize == null ? 16 * 1024 * 1024 : builder.maxInMemorySize;
        this.connectTimeoutMillis = builder.connectTimeoutMillis == null ? 5000 : builder.connectTimeoutMillis;
        this.chatResponseTimeoutMillis = builder.responseTimeoutMillis == null ? 60000L : builder.responseTimeoutMillis;
        this.streamResponseTimeoutMillis = builder.streamResponseTimeoutMillis;
        this.maxConnections = builder.maxConnections == null ? 50 : builder.maxConnections;
        this.maxIdleTimeMillis = builder.maxIdleTimeMillis == null ? 30000L : builder.maxIdleTimeMillis;
        this.maxLifeTimeMillis = builder.maxLifeTimeMillis == null ? 300000L : builder.maxLifeTimeMillis;
    }

    public HttpRuntimeKey toKey(HttpRuntimeMode mode) {
        return new HttpRuntimeKey(
                maxInMemorySize,
                connectTimeoutMillis,
                chatResponseTimeoutMillis,
                streamResponseTimeoutMillis,
                mode
        );
    }

    public HttpRuntimeKey toChatKey() {
        return toKey(HttpRuntimeMode.CHAT);
    }

    public HttpRuntimeKey toStreamKey() {
        return toKey(HttpRuntimeMode.STREAM);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer maxInMemorySize;
        private Integer connectTimeoutMillis;
        private Long responseTimeoutMillis;
        private Long streamResponseTimeoutMillis;
        private Long maxIdleTimeMillis;
        private Long maxLifeTimeMillis;
        private Integer maxConnections;

        public Builder maxInMemorySize(Integer maxInMemorySize) {
            this.maxInMemorySize = maxInMemorySize;
            return this;
        }

        public Builder connectTimeoutMillis(Integer connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder responseTimeoutMillis(Long responseTimeoutMillis) {
            this.responseTimeoutMillis = responseTimeoutMillis;
            return this;
        }

        public Builder streamResponseTimeoutMillis(Long streamResponseTimeoutMillis) {
            this.streamResponseTimeoutMillis = streamResponseTimeoutMillis;
            return this;
        }

        public Builder maxIdleTimeMillis(Long maxIdleTimeMillis) {
            this.maxIdleTimeMillis = maxIdleTimeMillis;
            return this;
        }

        public Builder maxLifeTimeMillis(Long maxLifeTimeMillis) {
            this.maxLifeTimeMillis = maxLifeTimeMillis;
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public HttpRuntimeConfig build() {
            validate();
            return new HttpRuntimeConfig(this);
        }

        private void validate() {
            if (maxInMemorySize != null && maxInMemorySize <= 0) {
                throw new IllegalArgumentException("maxInMemorySize must be greater than 0");
            }
            if (connectTimeoutMillis != null && connectTimeoutMillis <= 0) {
                throw new IllegalArgumentException("connectTimeoutMillis must be greater than 0");
            }
            if (responseTimeoutMillis != null && responseTimeoutMillis <= 0) {
                throw new IllegalArgumentException("responseTimeoutMillis must be greater than 0");
            }
            if (streamResponseTimeoutMillis != null && streamResponseTimeoutMillis <= 0) {
                throw new IllegalArgumentException("streamResponseTimeoutMillis must be greater than 0");
            }
        }
    }
}
