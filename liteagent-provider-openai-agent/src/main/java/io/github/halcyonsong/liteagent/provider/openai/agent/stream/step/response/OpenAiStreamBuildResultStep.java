package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

/**
 * 流式最终结果构建步骤。
 * <p>
 * 当前最小实现只负责补全 terminationReason。
 * 后续可在这里统一触发记忆窗口沉淀、trace 收口等逻辑。
 */
@Slf4j
public class OpenAiStreamBuildResultStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getTerminationReason() == null) {
            context.setTerminationReason(AgentTerminationReason.COMPLETED);
        }

        log.debug(
                "Built stream result. " +
                        "executionId={}, " +
                        "iteration={}, " +
                        "terminationReason={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getTerminationReason()
        );

        return StreamStepKey.END;
    }
}