package io.github.halcyonsong.liteagent.core.support;

/**
 * 提供统一 JSON 序列化能力的模型对象接口，配置由 {@link JsonSupport} 管理。
 */
public interface JsonSerializable {

    default String toJson() {
        return JsonSupport.toJson(this);
    }

    default String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }
}