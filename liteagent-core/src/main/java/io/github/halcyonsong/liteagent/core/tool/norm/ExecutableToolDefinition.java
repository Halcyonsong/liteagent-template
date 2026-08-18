package io.github.halcyonsong.liteagent.core.tool.norm;

import java.lang.reflect.Method;

/**
 * 可执行工具定义，保留静态工具描述和运行时反射执行所需的 target / method。
 */
public interface ExecutableToolDefinition extends ToolDefinition {

    Object getTarget();

    Method getMethod();
}