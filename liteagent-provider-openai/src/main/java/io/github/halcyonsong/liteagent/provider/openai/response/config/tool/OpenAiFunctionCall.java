package io.github.halcyonsong.liteagent.provider.openai.response.config.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;

/**
 * OpenAI-compatible 工具调用中的 function 信息。
 */
public class OpenAiFunctionCall {

    private final String name;
    private final String arguments;

    public OpenAiFunctionCall(String name, String arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public String getArguments() {
        return arguments;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiFunctionCall{" +
                "name='" + name + '\'' +
                ", arguments='" + arguments + '\'' +
                '}';
    }
}