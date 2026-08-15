package io.github.halcyonsong.liteagent.core.tool.norm;

import java.lang.reflect.Method;

/**
 * 可执行工具定义。
 * <p>
 * 既保留给模型发送的静态工具描述，
 * 也保留运行时反射执行所需的 target / method。
 */
public interface ExecutableToolDefinition extends ToolDefinition {

    Object getTarget();

    Method getMethod();
}