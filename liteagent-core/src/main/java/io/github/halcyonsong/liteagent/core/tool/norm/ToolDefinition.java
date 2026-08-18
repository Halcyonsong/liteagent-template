package io.github.halcyonsong.liteagent.core.tool.norm;

import java.util.Map;

/**
 * 工具定义规范，描述工具名称、说明和参数 JSON Schema。
 */
public interface ToolDefinition {

    String getName();

    String getDescription();

    Map<String, Object> getParameters();
}