package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 工具执行结果转换辅助类，调用 ToolExecutor 并将结果转换为可回写到 workingMessages 的 ToolMessage。
 */
public final class OpenAiToolExecutionSupport implements JsonSerializable {

    /** 工具执行默认总超时（毫秒），0 表示不限制。 */
    public static final long DEFAULT_TOOL_TIMEOUT_MILLIS = 120_000L;

    private OpenAiToolExecutionSupport() {
    }

    /**
     * 并行执行工具请求（不限制超时）。
     */
    public static List<ToolMessage> executeToMessages(
            List<ToolExecutionRequest> requests,
            ToolExecutor executor,
            ToolRegistry registry
    ) {
        return executeToMessages(requests, executor, registry, DEFAULT_TOOL_TIMEOUT_MILLIS);
    }

    /**
     * 并行执行工具请求，每个调用限制在 timeoutMillis 内完成（0 表示不限制）。
     */
    public static List<ToolMessage> executeToMessages(
            List<ToolExecutionRequest> requests,
            ToolExecutor executor,
            ToolRegistry registry,
            long timeoutMillis
    ) {
        Objects.requireNonNull(requests, "requests must not be null");
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return new ToolExecutionCoordinator(executor, registry, timeoutMillis)
                .executeAll(requests);
    }

    /**
     * 执行单个工具请求（同步）。
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
        return new ToolMessage(stringifyResult(result), request.getId());
    }

    /**
     * 将工具执行结果转换为消息 content。
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
}