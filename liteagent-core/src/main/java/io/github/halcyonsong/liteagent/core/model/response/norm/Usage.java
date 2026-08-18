package io.github.halcyonsong.liteagent.core.model.response.norm;

import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 模型调用的 token 用量信息。
 */
@Getter
@ToString
@AllArgsConstructor
public class Usage implements JsonSerializable {

    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

}