package io.github.halcyonsong.liteagent.core.model.response.norm;

/**
 * 统一的模型响应基础信息接口，描述响应 id、对象类型、创建时间和模型名称。
 */
public interface BaseResponse {

    String getId();

    String getObject();

    Long getCreated();

    String getModel();

}