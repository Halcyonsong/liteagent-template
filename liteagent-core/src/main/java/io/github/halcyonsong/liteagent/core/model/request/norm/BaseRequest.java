package io.github.halcyonsong.liteagent.core.model.request.norm;

/**
 * 统一的模型调用基础请求接口，定义接口地址、鉴权和模型名称。
 */
public interface BaseRequest {

    String getBaseUrl();

    String getApiKey();

    String getModel();
}