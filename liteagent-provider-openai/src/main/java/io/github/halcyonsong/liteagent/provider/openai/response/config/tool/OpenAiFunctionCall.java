package io.github.halcyonsong.liteagent.provider.openai.response.config.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * OpenAI-compatible 工具调用中的 function 信息。
 */
@Getter
@ToString
@AllArgsConstructor
public class OpenAiFunctionCall {

    private final String name;
    private final String arguments;

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}