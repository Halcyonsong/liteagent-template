package io.github.halcyonsong.liteagent.provider.openai.response.config;

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
}