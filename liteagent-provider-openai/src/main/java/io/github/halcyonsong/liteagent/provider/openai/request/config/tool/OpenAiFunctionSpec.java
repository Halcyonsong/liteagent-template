package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible function tool 定义。
 */
@Getter
@Builder
public class OpenAiFunctionSpec {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;

    public Map<String, Object> toRawValue() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);

        if (description != null && !description.isBlank()) {
            result.put("description", description);
        }

        result.put("parameters", parameters);
        return result;
    }

    public static OpenAiFunctionSpec of(String name, String description, Map<String, Object> parameters) {
        Objects.requireNonNull(name, "name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        Objects.requireNonNull(parameters, "parameters must not be null");

        return OpenAiFunctionSpec.builder()
                .name(name)
                .description(description == null ? "" : description)
                .parameters(parameters)
                .build();
    }
}