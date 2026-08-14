package io.github.halcyonsong.liteagent.agent.chat;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;

import java.util.Objects;

/**
 * 同步 chat 编排入口。
 * <p>
 * 该类不关心具体 provider 协议细节，
 * 只负责基于 ChatAgentExecutor 启动一次完整的 chat 编排流程。
 * <p>
 * 当前实现面向单次普通对话请求，
 * 由调用方预先装配好步骤执行器后再发起调用。
 */
public final class ChatAgent {

    private final ChatAgentExecutor executor;

    public ChatAgent(ChatAgentExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * 执行一次完整的 chat 编排，并返回完整上下文。
     * <p>
     * 适合需要读取中间态、终止原因、attributes 扩展数据的场景。
     *
     * @param invocation 本次调用的统一输入对象
     * @return 执行完成后的上下文
     */
    public ChatAgentContext executeContext(Invocation invocation) {
        return executor.execute(ChatAgentContext.create(invocation));
    }

    /**
     * 执行一次完整的 chat 编排，并返回最终结果。
     * <p>
     * 该方法只暴露最终结果抽象，不直接暴露中间执行状态。
     *
     * @param invocation 本次调用的统一输入对象
     * @return 最终结果
     */
    public Result execute(Invocation invocation) {
        return executeContext(invocation).getResult();
    }
}