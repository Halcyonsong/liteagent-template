package io.github.halcyonsong.liteagent.provider.openai.response.config.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * OpenAI-compatible assistant message 中的 tool call 信息。
 */
@Getter
@ToString
@AllArgsConstructor
public class OpenAiToolCall {

    private final Integer index;
    private final String id;
    private final String type;
    private final OpenAiFunctionCall function;

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}