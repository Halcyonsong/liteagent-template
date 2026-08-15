package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

import java.util.Objects;

/**
 * 将当前轮暂存消息统一写入 workingMessages。
 *
 * <p>写入顺序固定为：</p>
 *
 * <pre>
 * assistant(tool_calls)
 * tool(result)
 * </pre>
 *
 * <p>没有工具结果时，本轮结束；
 * 有工具结果时，进入下一轮模型请求。</p>
 */
public class OpenAiStreamAppendMessagesStep
        implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        StreamRoundState roundState = context.currentRound();

        boolean hasToolMessages = !roundState.getPendingToolMessages().isEmpty();

        context.appendWorkingMessages(roundState.getPendingAssistantMessages());

        context.appendWorkingMessages(roundState.getPendingToolMessages());

        roundState.clearPendingMessages();

        if (!hasToolMessages) {
            return StreamStepKey.BUILD_RESULT;
        }

        /*
         * iteration 表示已经完成的工具循环数量。
         * 当前轮工具结果写入后，下一步准备发起下一轮模型请求。
         */
        context.incrementIteration();

        if (context.getIteration() >= context.getMaxIterations()) {
            context.setTerminationReason(
                    AgentTerminationReason.MAX_ITERATIONS_REACHED
            );
            return StreamStepKey.BUILD_RESULT;
        }

        return StreamStepKey.BEGIN;
    }
}