package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 工具执行结果转换辅助类。
 * <p>
 * 负责调用 ToolExecutor，并将结果转换为可回写到 workingMessages 的 ToolMessage。
 */
public final class OpenAiToolExecutionSupport {

    private OpenAiToolExecutionSupport() {
    }

    /**
     * 执行一组工具请求，并按请求顺序转换为 ToolMessage。
     */
    public static List<ToolMessage> executeToMessages(
            List<ToolExecutionRequest> requests,
            ToolExecutor executor,
            ToolRegistry registry
    ) {
        Objects.requireNonNull(requests, "requests must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolMessage> messages = new ArrayList<>(requests.size());

        for (ToolExecutionRequest request : requests) {
            messages.add(executeToMessage(request, executor, registry));
        }

        return List.copyOf(messages);
    }

    /**
     * 执行单个工具请求，并构造对应的 ToolMessage。
     */
    public static ToolMessage executeToMessage(
            ToolExecutionRequest request,
            ToolExecutor executor,
            ToolRegistry registry
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        Objects.requireNonNull(registry, "registry must not be null");

        Object result = executor.execute(request, registry);
        String content = stringifyResult(result);

        return new ToolMessage(content, request.getId());
    }

    /**
     * 将工具执行结果转换为消息 content。
     * 字符串直接返回，其他类型统一序列化为 JSON。
     */
    public static String stringifyResult(Object result) {
        if (result == null) {
            return "null";
        }

        if (result instanceof String stringResult) {
            return stringResult;
        }

        return JsonSupport.toJson(result);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

}