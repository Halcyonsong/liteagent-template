package io.github.halcyonsong.liteagent.core.model.response.stream;

import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;
import io.github.halcyonsong.liteagent.core.model.response.Usage;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * 一次流式响应中的单个 chunk。
 * <p>
 * 该对象用于承载一次流式返回片段的基础响应信息、choice 列表以及可选 token 用量。
 */
@Getter
@ToString
public class StreamChunk {

    /**
     * 当前 chunk 的基础响应信息。
     */
    private final BaseResponse baseResponse;

    /**
     * 当前 chunk 中包含的候选片段列表。
     */
    private final List<StreamChoice> choices;

    /**
     * 当前 chunk 附带的 token 用量信息。
     * <p>
     * 不同供应商对流式 usage 的返回策略不一致，
     * 该字段可能为 null，也可能仅在最后一个 chunk 返回。
     */
    private final Usage usage;

    public StreamChunk(BaseResponse baseResponse,
                       List<StreamChoice> choices,
                       Usage usage) {
        this.baseResponse = Objects.requireNonNull(baseResponse, "baseResponse must not be null");
        Objects.requireNonNull(choices, "choices must not be null");
        this.choices = List.copyOf(choices);
        this.usage = usage;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}