package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * OpenAI 流式工具执行步骤。
 * <p>
 * 当前仅作为节点占位，不引入具体工具执行逻辑。
 * 后续可在此步骤中读取当前轮 finalResponse，执行工具并更新 workingMessages。
 */
public class OpenAiStreamExecuteToolStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        return StreamStepKey.END;
    }
}