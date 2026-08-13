package io.github.halcyonsong.liteagent.core.model.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 统一的聊天调用可选参数。
 * <p>
 * 该对象用于存放跨供应商相对稳定的基础生成控制项，
 * 例如温度参数以及最大生成 token 数。
 * 供应商特有扩展参数不应放入该类。
 */
@Getter
@Builder
public class ChatOptions implements BaseOptions {

    /**
     * 采样温度，用于控制输出随机性。
     */
    private final Double temperature;

    /**
     * 最大生成 token 数限制。
     */
    private final Integer maxTokens;

}