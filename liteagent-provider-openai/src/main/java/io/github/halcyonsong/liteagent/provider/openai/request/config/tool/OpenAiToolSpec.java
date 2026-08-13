package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible tools 字段中的单个工具定义。
 */
@Getter
@Builder
public class OpenAiToolSpec {

    /**
     * 当前先只支持 function。
     */
    private final String type;

    /**
     * function 类型工具定义。
     */
    private final OpenAiFunctionSpec function;

    public Map<String, Object> toRawValue() {
        return Map.of(
                "type", type,
                "function", function.toRawValue()
        );
    }

    public static OpenAiToolSpec function(OpenAiFunctionSpec function) {
        Objects.requireNonNull(function, "function must not be null");
        return OpenAiToolSpec.builder()
                .type("function")
                .function(function)
                .build();
    }

}