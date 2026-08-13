package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible function tool 定义。
 */
@Getter
@Builder
public class OpenAiFunctionSpec {

    /**
     * 工具名称。
     */
    private final String name;

    /**
     * 工具描述。
     */
    private final String description;

    /**
     * JSON Schema 参数定义。
     */
    private final Map<String, Object> parameters;

    public Map<String, Object> toRawValue() {
        return Map.of(
                "name", name,
                "description", description,
                "parameters", parameters
        );
    }

    public static OpenAiFunctionSpec of(String name, String description, Map<String, Object> parameters) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        Objects.requireNonNull(parameters, "parameters must not be null");

        return OpenAiFunctionSpec.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .build();
    }

}