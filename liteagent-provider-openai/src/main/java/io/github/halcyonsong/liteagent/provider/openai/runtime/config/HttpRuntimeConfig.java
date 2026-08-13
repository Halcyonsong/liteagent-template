package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

import lombok.Getter;

/**
 * 基础 WebClient 运行时配置。
 * <p>
 * 该配置用于控制底层 HTTP 客户端实例的运行时行为，
 * 仅包含与网络传输和响应解码相关的基础参数，
 * 不包含 provider 地址、鉴权信息或单次模型请求参数。
 */
@Getter
public class HttpRuntimeConfig {

    /**
     * 单次响应体允许聚合到内存中的最大字节数。
     * <p>
     * 该值用于限制 WebClient 解码响应体时的最大缓冲区大小，
     * 默认值为 16MB。
     */
    private final Integer maxInMemorySize;

    /**
     * 连接超时时间，单位毫秒。
     * <p>
     * 该值控制建立 TCP 连接的最大等待时间，
     * 默认值为 5000 毫秒。
     */
    private final Integer connectTimeoutMillis;

    /**
     * 响应超时时间，单位毫秒。
     * <p>
     * 该值控制请求发出后等待响应完成的最大时间，
     * 默认值为 60000 毫秒。
     */
    private final Long responseTimeoutMillis;


    /**
     * 流式响应超时时间，单位毫秒。
     * <p>
     * 该值控制请求发出后等待流式响应完成的最大时间，
     * 默认不设置超时时间。
     */
    private final Long streamResponseTimeoutMillis;

    private HttpRuntimeConfig(Builder builder) {
        this.maxInMemorySize = builder.maxInMemorySize == null
                ? 16 * 1024 * 1024
                : builder.maxInMemorySize;

        this.connectTimeoutMillis = builder.connectTimeoutMillis == null
                ? 5000
                : builder.connectTimeoutMillis;

        this.responseTimeoutMillis = builder.responseTimeoutMillis == null
                ? 60000L
                : builder.responseTimeoutMillis;
        this.streamResponseTimeoutMillis = builder.streamResponseTimeoutMillis;
    }

    public HttpRuntimeKey toKey(HttpRuntimeMode mode) {
        return new HttpRuntimeKey(
                maxInMemorySize,
                connectTimeoutMillis,
                responseTimeoutMillis,
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
