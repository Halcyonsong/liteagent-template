package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * OpenAI 流式轮次结束后的下一步决策节点。
 * <p>
 * 当前最小实现默认进入 BUILD_RESULT。
 * <p>
 * 后续可在此节点中基于当前轮 finalResponse 中聚合后的：
 * 1. assistant content
 * 2. reasoningContent
 * 3. toolCalls
 * 4. finishReason
 * 决定进入 BUILD_RESULT 还是 EXECUTE_TOOL。
 */
public class OpenAiStreamDecideNextActionStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        return StreamStepKey.BUILD_RESULT;
    }
}