package io.github.halcyonsong.liteagent.provider.openai.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI provider 内部 JSON 工具。
 * <p>
 * 统一复用单例 ObjectMapper，避免在 transport 等高频路径中重复创建。
 */
public final class OpenAiJsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OpenAiJsonSupport() {
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }
}