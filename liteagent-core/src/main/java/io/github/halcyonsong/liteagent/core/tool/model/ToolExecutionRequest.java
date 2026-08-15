package io.github.halcyonsong.liteagent.core.tool.model;

import io.github.halcyonsong.liteagent.core.exception.ToolExecutionException;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 统一工具执行请求。
 */
@Getter
@ToString
public class ToolExecutionRequest {

    private final Integer index;
    private final String id;
    private final String type;
    private final FunctionCall function;

    public ToolExecutionRequest(Integer index, String id, String type, FunctionCall function) {
        this.index = index;
        this.id = id;
        this.type = type;
        this.function = Objects.requireNonNull(function, "function must not be null");
    }

    public static ToolExecutionRequest from(ToolCall toolCall) {
        Objects.requireNonNull(toolCall, "toolCall must not be null");

        if (toolCall.getFunction() == null) {
            throw new ToolExecutionException("Tool call function must not be null");
        }

        if (toolCall.getFunction().getName() == null || toolCall.getFunction().getName().isBlank()) {
            throw new ToolExecutionException("Tool call function name must not be blank");
        }

        return new ToolExecutionRequest(
                toolCall.getIndex(),
                toolCall.getId(),
                toolCall.getType(),
                toolCall.getFunction()
        );
    }

    public String getToolName() {
        return function.getName();
    }

    public String getArgumentsJson() {
        return function.getArguments();
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

}