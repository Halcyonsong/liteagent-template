package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * OpenAI 流式编排起始步骤。
 * <p>
 * 第 0 轮先初始化工作态消息；
 * 后续轮次直接基于已有 workingMessages 构造请求。
 */
public class OpenAiStreamBeginStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getIteration() == 0 && context.getWorkingMessages().isEmpty()) {
            return StreamStepKey.INIT_WORKING_MESSAGES;
        }
        return StreamStepKey.MAP_REQUEST;
    }
}