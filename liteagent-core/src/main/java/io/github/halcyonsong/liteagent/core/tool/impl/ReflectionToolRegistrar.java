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
 * 基于反射的可执行工具定义。
 * <p>
 * 既用于向模型暴露 schema，也用于运行时反射执行。
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
            throw new IllegalArgumentException("method does not belong to toolObject type: " + method.getName());
        }

        ToolMethod toolMethod = method.getAnnotation(ToolMethod.class);
        ExecutableToolDefinition definition = buildDefinition(toolObject, method, toolMethod);
        registry.register(definition);
    }

    private ExecutableToolDefinition buildDefinition(Object toolObject, Method method, ToolMethod toolMethod) {
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

            Map<String, Object> schema = resolveJsonSchema(parameter, parameter.getType());
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

        throw new IllegalStateException("Parameter name not found, please compile with -parameters or set @ToolParam.name");
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

    private Map<String, Object> resolveJsonSchema(Parameter parameter, Class<?> type) {
        Map<String, Object> schema = new LinkedHashMap<>();

        // 基本类型
        if (type == String.class) {
            schema.put("type", "string");
        } else if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class) {
            schema.put("type", "integer");
        } else if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            schema.put("type", "number");
        } else if (type == Boolean.class || type == boolean.class) {
            schema.put("type", "boolean");
        }
        // 数组类型
        else if (type.isArray()) {
            schema.put("type", "array");
            schema.put("items", resolveJsonSchema(parameter, type.getComponentType()));
        }
        // List / Collection（泛型擦除后取第一个泛型参数）
        else if (List.class.isAssignableFrom(type) || Iterable.class.isAssignableFrom(type)) {
            schema.put("type", "array");
            schema.put("items", resolveListItemSchema(parameter)); // 无法可靠推断泛型，默认 string
        }
        // Map
        else if (Map.class.isAssignableFrom(type)) {
            schema.put("type", "object");
        }
        // 枚举
        else if (type.isEnum()) {
            schema.put("type", "string");
            Object[] constants = type.getEnumConstants();
            List<String> enumValues = new ArrayList<>(constants.length);
            for (Object constant : constants) {
                enumValues.add(((Enum<?>) constant).name());
            }
            schema.put("enum", enumValues);
        }
        // 其他自定义结构体：暂不展开，退化为 string
        else {
            schema.put("type", "string");
        }

        return schema;
    }

    private Map<String, Object> resolveListItemSchema(Parameter parameter) {
        java.lang.reflect.Type genericType = parameter.getParameterizedType();
        if (genericType instanceof java.lang.reflect.ParameterizedType pt) {
            java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> itemClass) {
                return resolveJsonSchema(parameter, itemClass);
            }
        }
        // 无法推断时退化为 string
        return Map.of("type", "string");
    }

}