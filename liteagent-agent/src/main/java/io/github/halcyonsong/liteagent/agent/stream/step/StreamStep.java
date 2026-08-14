package io.github.halcyonsong.liteagent.agent.stream.step;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;

/**
 * 流式编排中的流步骤。
 * <p>
 * 负责基于上游流对象继续包装、增强或分析，并返回下一步。
 *
 * @param <T> 当前流步骤内部传递的数据类型
 */
@FunctionalInterface
public interface StreamStep<T> {

    /**
     * 执行当前流步骤。
     *
     * @param upstream 上游流对象
     * @param context 本次流式编排上下文
     * @return 当前步骤产出和下一步标识
     */
    StreamApplyResult<T> apply(T upstream, StreamAgentContext<?> context);
}