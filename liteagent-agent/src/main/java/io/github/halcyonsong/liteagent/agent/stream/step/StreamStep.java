package io.github.halcyonsong.liteagent.agent.stream.step;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;

/**
 * 流式编排中的流步骤，基于上游流对象继续包装、增强或分析并返回下一步。
 *
 * @param <T> 当前流步骤内部传递的数据类型
 */
@FunctionalInterface
public interface StreamStep<T> {

    StreamApplyResult<T> apply(T upstream, StreamAgentContext<?> context);
}