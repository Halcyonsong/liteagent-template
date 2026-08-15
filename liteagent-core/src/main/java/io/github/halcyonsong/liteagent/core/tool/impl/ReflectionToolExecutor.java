package io.github.halcyonsong.liteagent.core.tool.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ExecutableToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 基于反射的工具执行器。
 */
public class ReflectionToolExecutor implements ToolExecutor {

    private final ObjectMapper objectMapper;

    public ReflectionToolExecutor() {
        this(new ObjectMapper());
    }

    public ReflectionToolExecutor(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    @Override
    public Object execute(ToolExecutionRequest request, ToolRegistry registry) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        if (request.getType() != null && !"function".equals(request.getType())) {
            throw new ToolExecutionException("Unsupported tool call type: " + request.getType());
        }

        ToolDefinition definition = registry.get(request.getToolName());
        if (definition == null) {
            throw new ToolExecutionException("Tool not found in registry: " + request.getToolName());
        }
        if (!(definition instanceof ExecutableToolDefinition executableTool)) {
            throw new ToolExecutionException(
                    "Tool definition is not executable: " + request.getToolName()
            );
        }

        Map<String, Object> arguments = parseArguments(request.getArgumentsJson());
        Object[] methodArguments = bindArguments(executableTool.getMethod(), arguments);

        try {
            Method method = executableTool.getMethod();
            method.setAccessible(true);
            return method.invoke(executableTool.getTarget(), methodArguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException() == null ? e : e.getTargetException();
            throw new ToolExecutionException(
                    "Failed to execute tool: " + request.getToolName(),
                    cause
            );
        } catch (Exception e) {
            throw new ToolExecutionException(
                    "Failed to execute tool: " + request.getToolName(),
                    e
            );
        }
    }

    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(
                    argumentsJson,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            throw new ToolExecutionException(
                    "Failed to parse tool arguments JSON: " + argumentsJson,
                    e
            );
        }
    }

    private Object[] bindArguments(Method method, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] result = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String parameterName = resolveParameterName(parameter);

            Object rawValue = arguments.get(parameterName);
            if (rawValue == null && isRequired(parameter)) {
                throw new ToolExecutionException(
                        "Missing required tool argument: " + parameterName
                );
            }

            result[i] = convertValue(rawValue, parameter.getType(), parameterName);
        }

        return result;
    }

    private String resolveParameterName(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        if (toolParam != null && !toolParam.name().isBlank()) {
            return toolParam.name();
        }

        if (parameter.isNamePresent()) {
            return parameter.getName();
        }

        throw new ToolExecutionException(
                "Parameter name not found, please compile with -parameters or set @ToolParam.name"
        );
    }

    private boolean isRequired(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        return toolParam == null || toolParam.required();
    }

    private Object convertValue(
            Object rawValue,
            Class<?> targetType,
            String parameterName
    ) {
        if (rawValue == null) {
            if (targetType.isPrimitive()) {
                throw new ToolExecutionException(
                        "Primitive parameter cannot be null: "
                                + parameterName
                );
            }
            return null;
        }

        try {
            if (targetType == String.class) {
                return String.valueOf(rawValue);
            }

            if (targetType == Integer.class || targetType == int.class) {
                return toInteger(rawValue, parameterName);
            }

            if (targetType == Long.class || targetType == long.class) {
                return toLong(rawValue, parameterName);
            }

            if (targetType == Double.class || targetType == double.class) {
                return toDouble(rawValue, parameterName);
            }

            if (targetType == Float.class || targetType == float.class) {
                return toFloat(rawValue, parameterName);
            }

            if (targetType == Boolean.class || targetType == boolean.class) {
                return toBoolean(rawValue, parameterName);
            }

            /*
             * 复杂类型交给 Jackson 处理：
             * - enum
             * - List / Map
             * - 自定义 POJO
             */
            return objectMapper.convertValue(rawValue, targetType);
        } catch (IllegalArgumentException e) {
            throw new ToolExecutionException(
                    "Failed to convert argument to "
                            + targetType.getName()
                            + ": "
                            + parameterName,
                    e
            );
        }
    }

    private Integer toInteger(Object rawValue, String parameterName) {
        if (rawValue instanceof Integer i) {
            return i;
        }
        if (rawValue instanceof Number number) {
            return number.intValue();
        }
        if (rawValue instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new ToolExecutionException(
                        "Failed to convert argument to Integer: " + parameterName,
                        e
                );
            }
        }
        throw new ToolExecutionException("Unsupported Integer argument type: " + parameterName);
    }

    private Long toLong(Object rawValue, String parameterName) {
        if (rawValue instanceof Long l) {
            return l;
        }
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        if (rawValue instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ToolExecutionException(
                        "Failed to convert argument to Long: " + parameterName,
                        e
                );
            }
        }
        throw new ToolExecutionException("Unsupported Long argument type: " + parameterName);
    }

    private Double toDouble(Object rawValue, String parameterName) {
        if (rawValue instanceof Double d) {
            return d;
        }
        if (rawValue instanceof Number number) {
            return number.doubleValue();
        }
        if (rawValue instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new ToolExecutionException(
                        "Failed to convert argument to Double: " + parameterName,
                        e
                );
            }
        }
        throw new ToolExecutionException("Unsupported Double argument type: " + parameterName);
    }

    private Float toFloat(Object rawValue, String parameterName) {
        if (rawValue instanceof Float f) {
            return f;
        }
        if (rawValue instanceof Number number) {
            return number.floatValue();
        }
        if (rawValue instanceof String s) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                throw new ToolExecutionException(
                        "Failed to convert argument to Float: " + parameterName,
                        e
                );
            }
        }
        throw new ToolExecutionException("Unsupported Float argument type: " + parameterName);
    }

    private Boolean toBoolean(Object rawValue, String parameterName) {
        if (rawValue instanceof Boolean b) {
            return b;
        }
        if (rawValue instanceof String s) {
            if ("true".equalsIgnoreCase(s)) {
                return true;
            }
            if ("false".equalsIgnoreCase(s)) {
                return false;
            }
        }
        throw new ToolExecutionException("Unsupported Boolean argument type: " + parameterName);
    }
}