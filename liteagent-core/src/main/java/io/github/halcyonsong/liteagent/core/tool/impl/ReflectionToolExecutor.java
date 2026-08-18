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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 基于反射的工具执行器。
 */
public class ReflectionToolExecutor implements ToolExecutor {

    private static final int MAX_PREVIEW_LENGTH = 200;

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
            throw new ToolExecutionException("Tool definition is not executable: " + request.getToolName());
        }

        Map<String, Object> arguments = parseArguments(request.getArgumentsJson());
        Object[] methodArguments = bindArguments(executableTool.getMethod(), arguments);

        try {
            Method method = executableTool.getMethod();
            method.setAccessible(true);
            return method.invoke(executableTool.getTarget(), methodArguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException() == null ? e : e.getTargetException();
            throw new ToolExecutionException("Failed to execute tool: " + request.getToolName(), cause);
        } catch (Exception e) {
            throw new ToolExecutionException("Failed to execute tool: " + request.getToolName(), e);
        }
    }


    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(argumentsJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new ToolExecutionException(
                    "Failed to parse tool arguments JSON (length: "
                            + argumentsJson.length()
                            + ", preview: "
                            + truncateForPreview(argumentsJson)
                            + ")",
                    e
            );
        }
    }

    /**
     * 截取 JSON 前缀用于异常消息，避免完整暴露过大或敏感内容。
     */
    private static String truncateForPreview(String json) {
        if (json.length() <= MAX_PREVIEW_LENGTH) {
            return json;
        }
        return json.substring(0, MAX_PREVIEW_LENGTH) + "...(truncated)";
    }

    private Object[] bindArguments(Method method, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] result = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String parameterName = resolveParameterName(parameter);

            Object rawValue = arguments.get(parameterName);
            if (rawValue == null && isRequired(parameter)) {
                throw new ToolExecutionException("Missing required tool argument: " + parameterName);
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

        throw new ToolExecutionException("Parameter name not found, please compile with -parameters or set @ToolParam.name");
    }

    private boolean isRequired(Parameter parameter) {
        ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
        return toolParam == null || toolParam.required();
    }

    private Object convertValue(Object rawValue, Class<?> targetType, String parameterName) {
        if (rawValue == null) {
            if (targetType.isPrimitive()) {
                throw new ToolExecutionException("Primitive parameter cannot be null: " + parameterName);
            }
            return null;
        }

        try {
            if (targetType == String.class) {
                if (rawValue instanceof String stringValue) {
                    return stringValue;
                }
                return objectMapper.writeValueAsString(rawValue);
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
             * 基础类型走显式转换，复杂类型统一交给 Jackson。
             */
            return objectMapper.convertValue(rawValue, targetType);
        } catch (Exception e) {
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
        try {
            return toBigDecimal(rawValue, parameterName).intValueExact();
        } catch (ArithmeticException e) {
            throw new ToolExecutionException(
                    "Invalid Integer argument: " + parameterName
                            + ". Value must be an integer within ["
                            + Integer.MIN_VALUE + ", " + Integer.MAX_VALUE + "]",
                    e
            );
        }
    }

    private Long toLong(Object rawValue, String parameterName) {
        try {
            return toBigDecimal(rawValue, parameterName).longValueExact();
        } catch (ArithmeticException e) {
            throw new ToolExecutionException(
                    "Invalid Long argument: " + parameterName
                            + ". Value must be an integer within ["
                            + Long.MIN_VALUE + ", " + Long.MAX_VALUE + "]",
                    e
            );
        }
    }

    private Double toDouble(Object rawValue, String parameterName) {
        double value;
        try {
            if (rawValue instanceof Number number) {
                value = number.doubleValue();
            } else if (rawValue instanceof String stringValue) {
                value = Double.parseDouble(stringValue);
            } else {
                throw unsupportedNumberType("Double", parameterName, rawValue);
            }
        } catch (NumberFormatException e) {
            throw new ToolExecutionException(
                    "Failed to convert argument to Double: " + parameterName,
                    e
            );
        }

        if (!Double.isFinite(value)) {
            throw new ToolExecutionException(
                    "Invalid Double argument: " + parameterName + ". Value must be finite"
            );
        }
        return value;
    }

    private Float toFloat(Object rawValue, String parameterName) {
        float value;
        try {
            if (rawValue instanceof Number number) {
                value = number.floatValue();
            } else if (rawValue instanceof String stringValue) {
                value = Float.parseFloat(stringValue);
            } else {
                throw unsupportedNumberType("Float", parameterName, rawValue);
            }
        } catch (NumberFormatException e) {
            throw new ToolExecutionException(
                    "Failed to convert argument to Float: " + parameterName,
                    e
            );
        }

        if (!Float.isFinite(value)) {
            throw new ToolExecutionException(
                    "Invalid Float argument: " + parameterName + ". Value must be finite"
            );
        }
        return value;
    }

    private BigDecimal toBigDecimal(Object rawValue, String parameterName) {
        try {
            if (rawValue instanceof BigDecimal decimal) {
                return decimal;
            }
            if (rawValue instanceof BigInteger integer) {
                return new BigDecimal(integer);
            }
            if (rawValue instanceof Byte
                    || rawValue instanceof Short
                    || rawValue instanceof Integer
                    || rawValue instanceof Long) {
                return BigDecimal.valueOf(((Number) rawValue).longValue());
            }
            if (rawValue instanceof Float || rawValue instanceof Double) {
                double value = ((Number) rawValue).doubleValue();
                if (!Double.isFinite(value)) {
                    throw new ToolExecutionException(
                            "Invalid numeric argument: " + parameterName + ". Value must be finite"
                    );
                }
                return BigDecimal.valueOf(value);
            }
            if (rawValue instanceof String stringValue) {
                return new BigDecimal(stringValue);
            }
        } catch (NumberFormatException e) {
            throw new ToolExecutionException(
                    "Failed to convert argument to number: " + parameterName,
                    e
            );
        }

        throw unsupportedNumberType("numeric", parameterName, rawValue);
    }

    private ToolExecutionException unsupportedNumberType(
            String targetType,
            String parameterName,
            Object rawValue
    ) {
        return new ToolExecutionException(
                "Unsupported " + targetType + " argument type for "
                        + parameterName + ": " + rawValue.getClass().getName()
        );
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