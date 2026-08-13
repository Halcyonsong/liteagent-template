package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OpenAiSchemas {

    public static Map<String, Object> object(Map<String, Object> properties, String... required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (required != null && required.length > 0) {
            schema.put("required", List.of(required));
        }
        return schema;
    }

    public static Map<String, Object> string(String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        schema.put("description", description);
        return schema;
    }
}
