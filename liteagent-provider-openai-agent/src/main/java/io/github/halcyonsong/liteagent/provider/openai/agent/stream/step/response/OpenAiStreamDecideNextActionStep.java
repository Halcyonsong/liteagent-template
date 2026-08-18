package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 决定当前流式轮次结束后的下一步。
 */
@Slf4j
public class OpenAiStreamDecideNextActionStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        AgentTerminationReason terminationReason = context.getTerminationReason();

        if (terminationReason == AgentTerminationReason.MODEL_ERROR
                || terminationReason == AgentTerminationReason.TOOL_ERROR
                || terminationReason == AgentTerminationReason.CANCELLED) {
            log.warn(
                    "Ending stream round due to termination. execId={}, round={}, reason={}",
                    context.getExecutionId(),
                    context.currentRound().getRoundIndex(),
                    terminationReason
            );
            return StreamStepKey.END;
        }

        OpenAiStreamCompletionResponse response =
                OpenAiStreamToolCallSupport.getFinalResponse(context);

        StreamRoundState roundState = context.currentRound();
        roundState.clearPendingMessages();

        roundState.appendPendingAssistantMessages(
                OpenAiStreamToolCallSupport.collectAssistantMessages(response)
        );

        boolean hasToolCalls = OpenAiStreamToolCallSupport.hasToolCalls(response);
        int assistantMessageCount = roundState.getPendingAssistantMessages().size();
        StreamStepKey nextKey = hasToolCalls ? StreamStepKey.EXECUTE_TOOL : StreamStepKey.APPEND_MESSAGES;

        log.debug(
                "Decided next action. execId={}, round={}, reason={}, msgs={}, tools={}, next={}",
                context.getExecutionId(),
                roundState.getRoundIndex(),
                terminationReason,
                assistantMessageCount,
                hasToolCalls,
                hasToolCalls ? "EXECUTE_TOOL" : "APPEND_MESSAGES"
        );

        return nextKey;
    }
}