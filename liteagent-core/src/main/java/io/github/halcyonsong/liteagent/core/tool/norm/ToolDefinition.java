package io.github.halcyonsong.liteagent.core.tool.norm;

import java.util.Map;

/**
 * 工具定义规范。
 * <p>
 * 当前只描述工具的静态信息：
 * 名称、说明、参数 JSON Schema。
 * 后续真正执行工具时，可再补执行接口或执行器。
 */
public interface ToolDefinition {

    /**
     * 工具唯一名称。
     */
    String getName();

    /**
     * 工具描述。
     */
    String getDescription();

    /**
     * 工具参数 JSON Schema。
     */
    Map<String, Object> getParameters();
}