package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

import java.util.Objects;

/**
 * 基础 WebClient 运行时实例缓存键。
 * <p>
 * 所有会影响底层 WebClient 构造结果的基础运行时参数，
 * 都应参与该键的唯一性计算。
 */
public class HttpRuntimeKey {

    private final Integer maxInMemorySize;
    private final Integer connectTimeoutMillis;
    private final Long responseTimeoutMillis;
    private final Long streamResponseTimeoutMillis;
    private final HttpRuntimeMode mode;

    public HttpRuntimeKey(Integer maxInMemorySize,
                          Integer connectTimeoutMillis,
                          Long responseTimeoutMillis,
                          Long streamResponseTimeoutMillis,
                          HttpRuntimeMode mode) {
        this.maxInMemorySize = Objects.requireNonNull(maxInMemorySize, "maxInMemorySize must not be null");
        this.connectTimeoutMillis = Objects.requireNonNull(connectTimeoutMillis, "connectTimeoutMillis must not be null");
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.streamResponseTimeoutMillis = streamResponseTimeoutMillis;
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
    }

    public Integer getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public Integer getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public Long getResponseTimeoutMillis() {
        return responseTimeoutMillis;
    }

    public Long getStreamResponseTimeoutMillis() {
        return streamResponseTimeoutMillis;
    }

    public HttpRuntimeMode getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpRuntimeKey that)) {
            return false;
        }
        return Objects.equals(maxInMemorySize, that.maxInMemorySize)
                && Objects.equals(connectTimeoutMillis, that.connectTimeoutMillis)
                && Objects.equals(responseTimeoutMillis, that.responseTimeoutMillis)
                && Objects.equals(streamResponseTimeoutMillis, that.streamResponseTimeoutMillis)
                && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                maxInMemorySize,
                connectTimeoutMillis,
                responseTimeoutMillis,
                streamResponseTimeoutMillis,
                mode
        );
    }

    @Override
    public String toString() {
        return "HttpRuntimeKey{" +
                "maxInMemorySize=" + maxInMemorySize +
                ", connectTimeoutMillis=" + connectTimeoutMillis +
                ", responseTimeoutMillis=" + responseTimeoutMillis +
                ", streamResponseTimeoutMillis=" + streamResponseTimeoutMillis +
                ", mode=" + mode +
                '}';
    }
}