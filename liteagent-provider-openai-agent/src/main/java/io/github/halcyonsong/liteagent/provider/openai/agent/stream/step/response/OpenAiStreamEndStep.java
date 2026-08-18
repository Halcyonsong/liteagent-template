package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

/**
 * Stream 编排终止步骤，可用于收尾日志、metrics 上报或资源清理。
 */
@Slf4j
public class OpenAiStreamEndStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        log.debug(
                "Stream agent finished. execId={}, iter={}, reason={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getTerminationReason()
        );
        return StreamStepKey.END;
    }
}