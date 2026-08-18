package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 单条流式候选结果片段，对应一次流式 chunk 中的一条 choice。
 */
@Getter
@ToString
public class StreamChoice implements JsonSerializable {

    private final Integer index;
    private final StreamDelta delta;

    /** 大多数中间 chunk 中为 null，通常仅在最后阶段出现具体值。 */
    private final FinishReason finishReason;

    public StreamChoice(Integer index, StreamDelta delta, FinishReason finishReason) {
        this.index = index;
        this.delta = Objects.requireNonNull(delta, "delta must not be null");
        this.finishReason = finishReason;
    }

}