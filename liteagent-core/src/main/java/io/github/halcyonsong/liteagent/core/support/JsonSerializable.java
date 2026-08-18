package io.github.halcyonsong.liteagent.core.support;

/**
 * 提供统一 JSON 序列化能力的模型对象接口。
 *
 * <p>默认使用当前对象作为序列化目标，
 * 具体序列化配置由 {@link JsonSupport} 统一管理。</p>
 */
public interface JsonSerializable {

    /**
     * 以格式化 JSON 输出当前对象。
     *
     * @return 格式化后的 JSON 字符串
     */
    default String toJson() {
        return JsonSupport.toJson(this);
    }

    /**
     * 以紧凑 JSON 输出当前对象。
     *
     * @return 紧凑 JSON 字符串
     */
    default String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }
}