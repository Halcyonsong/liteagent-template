package io.github.halcyonsong.liteagent.core.tool.model;

import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 统一工具执行请求。
 * <p>
 * 该对象用于承载从模型响应中解析出的单个 tool call，
 * 供后续执行器消费。
 * <p>
 * 当前保留完整字段：
 * 1. index
 * 2. id
 * 3. type
 * 4. function
 * <p>
 * 同时提供便捷访问方法，方便执行层直接取工具名称和参数 JSON。
 */
@Getter
@ToString
public class ToolExecutionRequest {

    private final Integer index;
    private final String id;
    private final String type;
    private final FunctionCall function;

    public ToolExecutionRequest(Integer index,
                                String id,
                                String type,
                                FunctionCall function) {
        this.index = index;
        this.id = id;
        this.type = type;
        this.function = Objects.requireNonNull(function, "function must not be null");
    }

    public static ToolExecutionRequest from(ToolCall toolCall) {
        Objects.requireNonNull(toolCall, "toolCall must not be null");
        return new ToolExecutionRequest(
                toolCall.getIndex(),
                toolCall.getId(),
                toolCall.getType(),
                toolCall.getFunction()
        );
    }

    /**
     * 工具名称。
     */
    public String getToolName() {
        return function.getName();
    }

    /**
     * 工具参数 JSON 字符串。
     */
    public String getArgumentsJson() {
        return function.getArguments();
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }
}