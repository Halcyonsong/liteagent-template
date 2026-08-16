package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 将当前轮 pending assistant/tool 消息写入 workingMessages。
 * <p>
 * 无工具结果时进入 BUILD_RESULT；
 * 有工具结果时递增 iteration 并开始下一轮。
 * 有工具结果但超过 maxIterations 时走 BUILD_RESULT。
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
                    "Appended stream messages. " +
                            "executionId={}, " +
                            "roundIndex={}, " +
                            "iteration={}, " +
                            "appendedAssistantCount={}, " +
                            "appendedToolCount={}, " +
                            "workingMessageCount={}, " +
                            "nextStep={}",
                    context.getExecutionId(),
                    roundState.getRoundIndex(),
                    context.getIteration(),
                    pendingAssistantCount,
                    pendingToolCount,
                    context.getWorkingMessages().size(),
                    StreamStepKey.BUILD_RESULT.name()
            );
            return StreamStepKey.BUILD_RESULT;
        }

        context.incrementIteration();

        if (context.getIteration() >= context.getMaxIterations()) {
            context.setTerminationReason(
                    AgentTerminationReason.MAX_ITERATIONS_REACHED
            );

            log.debug(
                    "Appended stream messages and reached max iterations. " +
                            "executionId={}, " +
                            "roundIndex={}, " +
                            "iteration={}, " +
                            "appendedAssistantCount={}, " +
                            "appendedToolCount={}, " +
                            "workingMessageCount={}, " +
                            "nextStep={}",
                    context.getExecutionId(),
                    roundState.getRoundIndex(),
                    context.getIteration(),
                    pendingAssistantCount,
                    pendingToolCount,
                    context.getWorkingMessages().size(),
                    StreamStepKey.BUILD_RESULT.name()
            );

            return StreamStepKey.BUILD_RESULT;
        }

        log.debug(
                "Appended stream messages and continue next round. " +
                            "executionId={}, " +
                            "roundIndex={}, " +
                            "iteration={}, " +
                            "appendedAssistantCount={}, " +
                            "appendedToolCount={}, " +
                            "workingMessageCount={}, " +
                            "nextStep={}",
                context.getExecutionId(),
                roundState.getRoundIndex(),
                context.getIteration(),
                pendingAssistantCount,
                pendingToolCount,
                context.getWorkingMessages().size(),
                StreamStepKey.BEGIN.name()
        );

        return StreamStepKey.BEGIN;
    }
}