package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * OpenAI 流式编排起始步骤。
 */
public class OpenAiStreamBeginStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getIteration() == 0) {
            if (context.getWorkingMessages().isEmpty()) {
                return StreamStepKey.INIT_WORKING_MESSAGES;
            }
            if (context.getToolRegistry() == null) {
                return StreamStepKey.INIT_TOOL_REGISTRY;
            }
        }
        return StreamStepKey.MAP_REQUEST;
    }
}