package io.github.halcyonsong.liteagent.agent.stream;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 流式 agent 编排入口。
 * <p>
 * 该类不关心具体 provider 协议细节，
 * 只负责基于 StreamAgentExecutor 启动一次完整的流式编排流程。
 *
 * @param <T> provider 对外输出的单个流元素类型
 */
public final class StreamAgent<T> {

    private final StreamAgentExecutor<T> executor;

    public StreamAgent(StreamAgentExecutor<T> executor) {
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * 执行一次完整的流式编排准备，并返回完整上下文。
     * <p>
     * 上下文中包含对外输出的惰性流，以及轮次状态、工作态消息历史等运行时数据。
     */
    public StreamAgentContext<T> executeContext(Invocation invocation) {
        return executor.executeContext(StreamAgentContext.create(invocation));
    }

    /**
     * 执行一次完整的流式编排，并返回 provider 对外输出的流。
     * <p>
     * 这是业务侧消费流式结果的首选入口。
     */
    public Flux<T> execute(Invocation invocation) {
        return executor.execute(StreamAgentContext.create(invocation));
    }
}