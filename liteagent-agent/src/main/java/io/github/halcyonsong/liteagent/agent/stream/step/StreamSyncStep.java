package io.github.halcyonsong.liteagent.agent.stream.step;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;

/**
 * 流式编排中的同步步骤。
 * <p>
 * 负责处理不直接操作 Flux 的步骤，
 * 例如请求映射、请求增强、工具执行和结果构建。
 */
@FunctionalInterface
public interface StreamSyncStep {

    /**
     * 执行当前同步步骤，并返回下一步。
     *
     * @param context 本次流式编排上下文
     * @return 下一步标识
     */
    StreamStepKey invoke(StreamAgentContext<?> context);
}