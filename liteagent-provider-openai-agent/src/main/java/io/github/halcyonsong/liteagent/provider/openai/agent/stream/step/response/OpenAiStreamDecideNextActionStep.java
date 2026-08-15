package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

import java.util.List;
import java.util.Objects;

/**
 * 决定当前流式轮次结束后的下一步。
 *
 * <p>该步骤只处理当前轮聚合结果和 assistant 暂存消息，
 * 不执行工具，也不直接写入 workingMessages。</p>
 */
public class OpenAiStreamDecideNextActionStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        OpenAiStreamCompletionResponse response =
                OpenAiStreamToolCallSupport.getFinalResponse(context);

        StreamRoundState roundState = context.currentRound();

        // 防止重复进入同一轮时残留旧的暂存消息。
        roundState.clearPendingMessages();

        List<AssistantResponseMessage> assistantMessages =
                OpenAiStreamToolCallSupport.collectAssistantMessages(response);

        /*
         * 无论是否包含 tool_calls，assistant 响应都先进入当前轮暂存区。
         * 后续统一由 APPEND_MESSAGES 写入 workingMessages。
         */
        roundState.appendPendingAssistantMessages(assistantMessages);

        if (OpenAiStreamToolCallSupport.hasToolCalls(response)) {
            return StreamStepKey.EXECUTE_TOOL;
        }

        return StreamStepKey.APPEND_MESSAGES;
    }
}