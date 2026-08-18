package io.github.halcyonsong.liteagent.core.model.response.norm;

/**
 * 响应增强器，在 raw response 映射为 provider response 后进行增强或后处理。
 *
 * @param <RAW_RESP> 原始响应对象类型
 * @param <RESP> 映射后的响应对象类型
 */
public interface ResponseAdvisor<RAW_RESP, RESP> {

    /**
     * 增强响应。
     *
     * @param rawResponse 原始响应对象
     * @param response 映射后的响应对象
     */
    void enhance(RAW_RESP rawResponse, RESP response);
}