package io.github.halcyonsong.liteagent.agent.chat;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.Result;

import java.util.Objects;

/**
 * 同步 chat 编排入口，基于 ChatAgentExecutor 启动一次完整的 chat 编排流程。
 */
public final class ChatAgent {

    private final ChatAgentExecutor executor;

    public ChatAgent(ChatAgentExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * 执行一次完整的 chat 编排，返回包含中间态、终止原因和扩展数据的上下文。
     */
    public ChatAgentContext executeContext(Invocation invocation) {
        return executor.execute(ChatAgentContext.create(invocation));
    }

    /**
     * 执行一次完整的 chat 编排，只返回最终结果，不暴露中间状态。
     */
    public Result execute(Invocation invocation) {
        return executeContext(invocation).getResult();
    }
}