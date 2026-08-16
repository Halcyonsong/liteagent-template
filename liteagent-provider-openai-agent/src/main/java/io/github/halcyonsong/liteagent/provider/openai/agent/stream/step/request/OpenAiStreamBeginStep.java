package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI 流式编排起始步骤。
 */
@Slf4j
public class OpenAiStreamBeginStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getIteration() == 0) {
            if (context.getWorkingMessages().isEmpty()) {
                log.info("workingMessages is empty, init it");
                return StreamStepKey.INIT_WORKING_MESSAGES;
            }
            if (context.getToolRegistry() == null) {
                log.info("toolRegistry is null, init it");
                return StreamStepKey.INIT_TOOL_REGISTRY;
            }
        }
        log.info("map request");
        return StreamStepKey.MAP_REQUEST;
    }
}