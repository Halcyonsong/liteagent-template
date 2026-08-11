package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;

/**
 * OpenAI-compatible 响应的基础元信息实现。
 * <p>
 * 该类实现 core 层 {@link BaseResponse} 接口，
 * 用于承载当前 provider 映射后的基础响应元数据。
 */
public class OpenAiBaseResponse implements BaseResponse {

    private final String id;
    private final String object;
    private final Long created;
    private final String model;

    public OpenAiBaseResponse(String id, String object, Long created, String model) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getObject() {
        return object;
    }

    @Override
    public Long getCreated() {
        return created;
    }

    @Override
    public String getModel() {
        return model;
    }

}