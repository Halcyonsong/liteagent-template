package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.model.response.norm.BaseResponse;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * OpenAI-compatible 响应基础元信息，实现 core 层 {@link BaseResponse}。
 */
@Getter
@ToString
@AllArgsConstructor
public class OpenAiBaseResponse implements BaseResponse, JsonSerializable {

    private final String id;
    private final String object;
    private final Long created;
    private final String model;

}