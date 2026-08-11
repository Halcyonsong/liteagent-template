package io.github.halcyonsong.liteagent.core.model.response;

/**
 * 统一的模型响应基础信息接口。
 * <p>
 * 该接口描述一次模型响应中与供应商无关或较为稳定的公共元数据，
 * 例如响应 id、响应对象类型、创建时间以及模型名称。
 */
public interface BaseResponse {

    String getId();

    String getObject();

    Long getCreated();

    String getModel();

}