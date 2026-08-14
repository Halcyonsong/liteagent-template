package io.github.halcyonsong.liteagent.agent;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.executor.AgentExecutor;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;

import java.util.Objects;

/**
 * Agent 编排入口。
 * <p>
 * 该类本身不关心具体 provider 协议细节，
 * 只负责基于 {@link io.github.halcyonsong.liteagent.agent.executor.AgentExecutor}
 * 启动一次完整的 agent 执行流程。
 * <p>
 * 当前最小实现由调用方预先装配好步骤执行器，
 * 再通过 {@link #executeContext} 或 {@link #execute} 发起一次调用。
 */
public final class Agent {

    private final AgentExecutor executor;

    public Agent(AgentExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * 执行一次完整的 agent 编排，并返回完整上下文。
     * <p>
     * 适合需要读取中间态、终止原因、attributes 扩展数据的场景。
     *
     * @param invocation 本次调用的统一输入对象
     * @return 执行完成后的上下文
     */
    public AgentContext executeContext(Invocation invocation) {
        return executor.execute(AgentContext.create(invocation));
    }

    /**
     * 执行一次完整的 agent 编排，并返回最终结果。
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