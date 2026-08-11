package io.github.halcyonsong.liteagent.core.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSupport() {
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object to JSON", e);
        }
    }
}