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
 * OpenAI 工具执行结果转换辅助类。
 *
 * <p>该类负责将 core 层的 ToolExecutionRequest
 * 和 ToolExecutor 执行结果转换为 OpenAI 对话协议使用的 ToolMessage。</p>
 *
 * <p>工具调用 ID 始终来自 ToolExecutionRequest.id，
 * 业务方法返回值只会被序列化为 ToolMessage.content。</p>
 */
public final class OpenAiToolExecutionSupport {

    private OpenAiToolExecutionSupport() {
    }

    /**
     * 执行一组工具请求，并将结果转换为 ToolMessage。
     *
     * <p>当前按照请求顺序同步执行。任意一个工具执行失败时，
     * 异常会直接向上抛出，由调用方决定如何设置终止原因。</p>
     *
     * @param requests  工具调用请求
     * @param executor  工具执行器
     * @param registry  工具注册表
     * @return 按请求顺序生成的工具结果消息
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
     *
     * @param request  工具调用请求
     * @param executor 工具执行器
     * @param registry 工具注册表
     * @return 工具结果消息
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
     * 将工具业务结果转换为消息 content。
     *
     * <p>字符串结果直接使用；
     * 其他类型统一序列化为 JSON。</p>
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