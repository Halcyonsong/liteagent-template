package io.github.halcyonsong.liteagent.provider.openai.response.config.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;

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

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiToolCall{" +
                "index=" + index +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", function=" + function +
                '}';
    }
}