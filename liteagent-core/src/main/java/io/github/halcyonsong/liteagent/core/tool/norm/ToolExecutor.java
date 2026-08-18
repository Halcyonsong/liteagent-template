package io.github.halcyonsong.liteagent.core.tool.norm;

import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;

/**
 * 工具执行器规范。
 */
public interface ToolExecutor {

    Object execute(ToolExecutionRequest request, ToolRegistry registry);
}