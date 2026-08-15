package io.github.halcyonsong.liteagent.core.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSupport() {
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象转换为 JSON 字符串。
     * 主要用于调试，查看原始结构
     * @param value 要转换的对象
     * @return 对象的 JSON 字符串表示
     */
    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 将对象转换为紧凑的 JSON 字符串。
     * 主要用于快速查看
     * @param value 要转换的对象
     * @return 对象的紧凑 JSON 字符串表示
     */
    public static String toCompactJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize object to compact JSON", e);
        }
    }

}