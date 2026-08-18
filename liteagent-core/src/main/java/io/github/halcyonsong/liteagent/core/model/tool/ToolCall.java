package io.github.halcyonsong.liteagent.core.model.tool;

import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 统一工具调用结构。
 * <p>
 * chat 场景下 index 通常可为空；
 * stream 场景下可用于跨 chunk 聚合。
 */
@Getter
@ToString
@AllArgsConstructor
public class ToolCall implements JsonSerializable {

    private final Integer index;
    private final String id;
    private final String type;
    private final FunctionCall function;

}