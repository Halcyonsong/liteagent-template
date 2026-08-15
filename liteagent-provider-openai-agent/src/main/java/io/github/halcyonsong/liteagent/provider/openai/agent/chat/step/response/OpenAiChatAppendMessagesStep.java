package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;

import java.util.Objects;

/**
 * 将当前轮暂存的 assistant/tool 消息写入 workingMessages。
 *
 * <p>如果本轮存在工具执行结果，则进入下一轮模型请求；
 * 如果本轮没有工具结果，则说明模型已经完成本次响应，进入 BUILD_RESULT。</p>
 */
public class OpenAiChatAppendMessagesStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        /*
         * 在清空 pending 列表前记录本轮是否执行过工具。
         * 普通 assistant 响应只有 pendingAssistantMessages，
         * 工具调用轮次则同时存在 pendingToolMessages。
         */
        boolean hasToolMessages = !context.getPendingToolMessages().isEmpty();

        context.appendWorkingMessages(context.getPendingAssistantMessages());
        context.appendWorkingMessages(context.getPendingToolMessages());
        context.clearPendingMessages();

        if (!hasToolMessages) {
            return ChatStepKey.BUILD_RESULT;
        }

        context.incrementIteration();

        if (context.getIteration() >= context.getMaxIterations()) {
            context.setTerminationReason(
                    AgentTerminationReason.MAX_ITERATIONS_REACHED
            );
            return ChatStepKey.BUILD_RESULT;
        }

        return ChatStepKey.BEGIN;
    }
}