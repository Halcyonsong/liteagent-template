package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

/**
 * 流式最终结果构建步骤，当前只负责补全 terminationReason，后续可在此触发记忆窗口沉淀、trace 收口等逻辑。
 */
@Slf4j
public class OpenAiStreamBuildResultStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getTerminationReason() == null) {
            context.setTerminationReason(AgentTerminationReason.COMPLETED);
        }

        log.debug(
                "Built stream result. execId={}, iter={}, reason={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getTerminationReason()
        );

        return StreamStepKey.END;
    }
}