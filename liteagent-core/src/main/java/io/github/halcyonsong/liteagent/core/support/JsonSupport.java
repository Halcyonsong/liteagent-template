package io.github.halcyonsong.liteagent.core.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSupport() {
    }

    /** 创建一个继承默认配置、可由调用方独立定制的 ObjectMapper 副本。 */
    public static ObjectMapper createObjectMapper() {
        return OBJECT_MAPPER.copy();
    }

    /** 主要用于调试，查看原始结构。 */
    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }

    /** 主要用于快速查看。 */
    public static String toCompactJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize object to compact JSON", e);
        }
    }

}