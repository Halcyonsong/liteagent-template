package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible tool_choice 请求字段。
 * <p>
 * 支持：
 * - none
 * - auto
 * - required
 * - 指定某个 function
 */
@Getter
public class OpenAiToolChoice {

    private final String mode;
    private final String functionName;

    private OpenAiToolChoice(String mode, String functionName) {
        this.mode = mode;
        this.functionName = functionName;
    }

    public static OpenAiToolChoice none() {
        return new OpenAiToolChoice("none", null);
    }

    public static OpenAiToolChoice auto() {
        return new OpenAiToolChoice("auto", null);
    }

    public static OpenAiToolChoice required() {
        return new OpenAiToolChoice("required", null);
    }

    public static OpenAiToolChoice function(String functionName) {
        Objects.requireNonNull(functionName, "functionName must not be null");
        return new OpenAiToolChoice("function", functionName);
    }

    public Object toRawValue() {
        if (!"function".equals(mode)) {
            return mode;
        }

        return Map.of(
                "type", "function",
                "function", Map.of("name", functionName)
        );
    }
}