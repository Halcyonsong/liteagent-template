package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ExecutableToolDefinition;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 基于反射的可执行工具定义。
 * <p>
 * 同时用于：
 * 1. 向模型暴露工具静态描述
 * 2. 运行时反射执行工具方法
 */
@Getter
public class ReflectiveToolDefinition implements ExecutableToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;
    private final Object target;
    private final Method method;

    public ReflectiveToolDefinition(String name,
                                    String description,
                                    Map<String, Object> parameters,
                                    Object target,
                                    Method method) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description == null ? "" : description;
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.method = Objects.requireNonNull(method, "method must not be null");
    }
}