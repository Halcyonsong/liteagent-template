package io.github.halcyonsong.liteagent.core.model.response.norm;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 模型调用的 token 用量信息。
 * <p>
 * 该对象用于记录一次请求中的提示词 token 数、生成 token 数以及总 token 数。
 */
@Getter
@ToString
@AllArgsConstructor
public class Usage {

    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}