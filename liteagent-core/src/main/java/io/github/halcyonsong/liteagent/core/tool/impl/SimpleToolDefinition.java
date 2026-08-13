package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 简单工具定义实现。
 */
@Getter
@Builder
public class SimpleToolDefinition implements ToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;
}