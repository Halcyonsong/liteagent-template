package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * OpenAI-compatible 响应的基础元信息实现。
 * <p>
 * 该类实现 core 层 {@link BaseResponse} 接口，
 * 用于承载当前 provider 映射后的基础响应元数据。
 */
@Getter
@ToString
@AllArgsConstructor
public class OpenAiBaseResponse implements BaseResponse {

    private final String id;
    private final String object;
    private final Long created;
    private final String model;

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}