package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 将当前轮 pending assistant/tool 消息写入 workingMessages。无工具结果时进入 BUILD_RESULT；有工具结果时递增 iteration 开始下一轮，超过 maxIterations 时走 BUILD_RESULT。
 */
@Slf4j
public class OpenAiStreamAppendMessagesStep
        implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        StreamRoundState roundState = context.currentRound();

        int pendingAssistantCount = roundState.getPendingAssistantMessages().size();
        int pendingToolCount = roundState.getPendingToolMessages().size();
        boolean hasToolMessages = pendingToolCount > 0;

        context.appendWorkingMessages(roundState.getPendingAssistantMessages());

        context.appendWorkingMessages(roundState.getPendingToolMessages());

        roundState.clearPendingMessages();

        if (!hasToolMessages) {
            log.debug(
                    "Appended messages. execId={}, round={}, iter={}, assistant={}, tool={}, total={}, next=BUILD_RESULT",
                    context.getExecutionId(),
                    roundState.getRoundIndex(),
                    context.getIteration(),
                    pendingAssistantCount,
                    pendingToolCount,
                    context.getWorkingMessages().size()
            );
            return StreamStepKey.BUILD_RESULT;
        }

        if (context.incrementIteration() >= context.getMaxIterations()) {
            context.setTerminationReason(
                    AgentTerminationReason.MAX_ITERATIONS_REACHED
            );

            log.debug(
                    "Appended messages, reached max iterations. execId={}, round={}, iter={}, assistant={}, tool={}, total={}, next=BUILD_RESULT",
                    context.getExecutionId(),
                    roundState.getRoundIndex(),
                    context.getIteration(),
                    pendingAssistantCount,
                    pendingToolCount,
                    context.getWorkingMessages().size()
            );

            return StreamStepKey.BUILD_RESULT;
        }

        log.debug(
                "Appended messages, continue next round. execId={}, round={}, iter={}, assistant={}, tool={}, total={}, next=BEGIN",
                context.getExecutionId(),
                roundState.getRoundIndex(),
                context.getIteration(),
                pendingAssistantCount,
                pendingToolCount,
                context.getWorkingMessages().size()
        );

        return StreamStepKey.BEGIN;
    }
}