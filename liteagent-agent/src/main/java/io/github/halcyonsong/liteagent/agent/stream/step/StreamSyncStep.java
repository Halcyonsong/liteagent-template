package io.github.halcyonsong.liteagent.agent.stream.step;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;

/**
 * 流式编排中的同步步骤，处理不直接操作 Flux 的步骤（请求映射、增强、工具执行、结果构建等）。
 */
@FunctionalInterface
public interface StreamSyncStep {

    StreamStepKey invoke(StreamAgentContext<?> context);
}