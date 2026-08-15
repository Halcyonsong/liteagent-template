package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.norm.ExecutableToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于反射的工具注册器。
 */
public class ReflectionToolRegistrar implements ToolRegistrar {

    @Override
    public void register(Object toolObject, ToolRegistry registry) {
        Objects.requireNonNull(toolObject, "toolObject must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        Class<?> toolClass = toolObject.getClass();
        if (!toolClass.isAnnotationPresent(ToolComponent.class)) {
            return;
        }

        for (Method method : toolClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(ToolMethod.class)) {
                continue;
            }
            register(toolObject, method, registry);
        }
    }

    @Override
    public void register(Object toolObject, Method method, ToolRegistry registry) {
        Objects.requireNonNull(toolObject, "toolObject must not be null");
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        if (!method.isAnnotationPresent(ToolMethod.class)) {
            throw new IllegalArgumentException("method is not annotated with @ToolMethod: " + method.getName());
        }

        if (!method.getDeclaringClass().isAssignableFrom(toolObject.getClass())) {
            throw new IllegalArgumentException(
                    "method does not belong to toolObject type: " + method.getName()
            );
        }

        ToolMethod toolMethod = method.getAnnotation(ToolMethod.class);
        ExecutableToolDefinition definition = buildDefinition(toolObject, method, toolMethod);
        registry.register(definition);
    }

    private ExecutableToolDefinition buildDefinition(Object toolObject,
                                                     Method method,
                                                     ToolMethod toolMethod) {
        Map<String, Object> parameters = buildParameters(method);

        return new ReflectiveToolDefinition(
                toolMethod.name(),
                toolMethod.description(),
                parameters,
                toolObject,
                method
        );
    }

    private Map<String, Object> buildParameters(Method method) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            String name = resolveParameterName(parameter);
            String description = resolveParameterDescription(parameter);
            boolean isRequired = resolveParameterRequired(parameter);

            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", resolveJsonType(parameter.getType()));
            if (!description.isBlank()) {
                schema.put("description", description);
            }

            properties.put(name, schema);
            if (isRequired) {
                required.add(name);
            }
        }

        parameters.put("type", "object");
        parameters.put("properties", properties);
        if (!required.isEmpty()) {
            parameters.put("required", required);
        }

        return parameters;
    }

    private String resolveParameterName(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        if (toolParam != null && !toolParam.name().isBlank()) {
            return toolParam.name();
        }

        if (parameter.isNamePresent()) {
            return parameter.getName();
        }

        throw new IllegalStateException(
                "Parameter name not found, please compile with -parameters or set @ToolParam.name"
        );
    }

    private String resolveParameterDescription(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        if (toolParam == null) {
            return "";
        }
        return toolParam.description() == null ? "" : toolParam.description();
    }

    private boolean resolveParameterRequired(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        return toolParam == null || toolParam.required();
    }

    private String resolveJsonType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class) {
            return "integer";
        }
        if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "number";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }
        return "string";
    }
}