package io.github.halcyonsong.liteagent.core.model.request.norm;

/**
 * 请求增强器，在 raw request 映射后、发送到 transport 前对请求进行增强。
 *
 * @param <R> 业务请求对象类型
 * @param <RAW> 原始发送请求对象类型
 */
public interface RequestAdvisor<R, RAW> {

    /**
     * 增强请求。
     *
     * @param request 业务请求对象
     * @param rawRequest 原始发送请求对象
     */
    void enhance(R request, RAW rawRequest);
}