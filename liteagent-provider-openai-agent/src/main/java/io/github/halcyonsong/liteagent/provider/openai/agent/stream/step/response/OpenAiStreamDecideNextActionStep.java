package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

import java.util.Objects;

/**
 * 决定当前流式轮次结束后的下一步。
 */
public class OpenAiStreamDecideNextActionStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        AgentTerminationReason terminationReason = context.getTerminationReason();

        if (terminationReason == AgentTerminationReason.MODEL_ERROR
                || terminationReason == AgentTerminationReason.TOOL_ERROR
                || terminationReason == AgentTerminationReason.CANCELLED) {
            return StreamStepKey.END;
        }

        OpenAiStreamCompletionResponse response =
                OpenAiStreamToolCallSupport.getFinalResponse(context);

        var roundState = context.currentRound();
        roundState.clearPendingMessages();

        roundState.appendPendingAssistantMessages(
                OpenAiStreamToolCallSupport.collectAssistantMessages(response)
        );

        if (OpenAiStreamToolCallSupport.hasToolCalls(response)) {
            return StreamStepKey.EXECUTE_TOOL;
        }

        return StreamStepKey.APPEND_MESSAGES;
    }
}