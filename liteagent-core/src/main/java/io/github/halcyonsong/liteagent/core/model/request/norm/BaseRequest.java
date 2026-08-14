package io.github.halcyonsong.liteagent.core.model.request.norm;

/**
 * 统一的模型调用基础请求接口。
 * <p>
 * 该接口定义一次模型调用所需的最小基础信息，
 * 包括目标接口地址、鉴权信息以及模型名称。
 * 具体供应商模块应提供自己的实现类。
 */
public interface BaseRequest {

    String getBaseUrl();

    String getApiKey();

    String getModel();
}