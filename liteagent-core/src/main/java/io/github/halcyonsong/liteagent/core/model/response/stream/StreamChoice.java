package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 单条流式候选结果片段。
 * <p>
 * 该对象对应一次流式 chunk 中的一条 choice，
 * 包含其索引位置、delta 增量内容以及结束原因。
 */
@Getter
@ToString
public class StreamChoice {

    /**
     * 当前候选片段在响应 choices 中的索引位置。
     */
    private final Integer index;

    /**
     * 当前候选片段对应的增量消息内容。
     */
    private final StreamDelta delta;

    /**
     * 当前候选片段的结束原因。
     * <p>
     * 大多数中间 chunk 中该字段为 null，
     * 通常仅在最后阶段出现具体值。
     */
    private final FinishReason finishReason;

    public StreamChoice(Integer index, StreamDelta delta, FinishReason finishReason) {
        this.index = index;
        this.delta = Objects.requireNonNull(delta, "delta must not be null");
        this.finishReason = finishReason;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

}