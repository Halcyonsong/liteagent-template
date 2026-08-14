package io.github.halcyonsong.liteagent.agent.stream.step;

import lombok.Getter;

import java.util.Objects;

/**
 * 流步骤执行结果。
 *
 * @param <T> 当前流步骤内部传递的数据类型
 */
@Getter
public final class StreamApplyResult<T> {

    private final T output;
    private final StreamStepKey nextKey;

    public StreamApplyResult(T output, StreamStepKey nextKey) {
        this.output = Objects.requireNonNull(output, "output must not be null");
        this.nextKey = Objects.requireNonNull(nextKey, "nextKey must not be null");
    }

}