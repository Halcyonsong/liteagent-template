package io.github.halcyonsong.liteagent.provider.openai.response.config;

/**
 * OpenAI-compatible assistant message 中的 tool call 信息。
 */
public class OpenAiToolCall {

    private final Integer index;
    private final String id;
    private final String type;
    private final OpenAiFunctionCall function;

    public OpenAiToolCall(Integer index, String id, String type, OpenAiFunctionCall function) {
        this.index = index;
        this.id = id;
        this.type = type;
        this.function = function;
    }

    public Integer getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public OpenAiFunctionCall getFunction() {
        return function;
    }
}