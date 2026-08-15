package io.github.halcyonsong.liteagent.core.tool.norm;

import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;

/**
 * 工具执行器规范。
 */
public interface ToolExecutor {

    /**
     * 执行单个工具调用请求。
     *
     * @param request  工具执行请求
     * @param registry 工具注册表
     * @return 执行结果
     */
    Object execute(ToolExecutionRequest request, ToolRegistry registry);
}