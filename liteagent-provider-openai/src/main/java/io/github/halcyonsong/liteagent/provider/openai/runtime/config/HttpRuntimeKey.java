package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 基础 WebClient 运行时实例缓存键。影响 WebClient 构造结果的参数均参与唯一性计算。
 */
@Getter
@ToString
@EqualsAndHashCode
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

}