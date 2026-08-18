package io.github.halcyonsong.liteagent.core.model.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 工具调用中的函数信息，承载函数名称与参数 JSON 字符串。
 */
@Getter
@ToString
@AllArgsConstructor
public class FunctionCall implements JsonSerializable {

    private final String name;
    private final String arguments;

}