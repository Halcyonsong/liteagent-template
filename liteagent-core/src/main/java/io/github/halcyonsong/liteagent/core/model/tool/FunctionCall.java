package io.github.halcyonsong.liteagent.core.model.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 工具调用中的函数信息。
 * <p>
 * 用于统一承载函数名称与参数 JSON 字符串。
 */
@Getter
@ToString
@AllArgsConstructor
public class FunctionCall {

    private final String name;
    private final String arguments;

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

}