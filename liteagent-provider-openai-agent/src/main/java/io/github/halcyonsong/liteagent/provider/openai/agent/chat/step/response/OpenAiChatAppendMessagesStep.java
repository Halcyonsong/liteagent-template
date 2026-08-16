package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 将当前轮 pending assistant/tool 消息按顺序写入 workingMessages。
 * <p>
 * 无工具结果时结束本轮并进入 BUILD_RESULT；
 * 有工具结果时递增 iteration 并进入下一轮。
 * 有工具结果但超过 maxIterations 时也走 BUILD_RESULT。
 */
@Slf4j
public class OpenAiChatAppendMessagesStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        boolean hasToolMessages = !context.getPendingToolMessages().isEmpty();

        int assistantCount = context.getPendingAssistantMessages().size();
        int toolCount = context.getPendingToolMessages().size();
        context.appendWorkingMessages(context.getPendingAssistantMessages());
        context.appendWorkingMessages(context.getPendingToolMessages());
        context.clearPendingMessages();

        log.debug(
                "Appended chat pending messages. " +
                        "executionId={}, " +
                        "iteration={}, " +
                        "appendedAssistantCount={}, " +
                        "appendedToolCount={}, " +
                        "workingMessageCount={}, " +
                        "hasToolMessages={}",
                context.getExecutionId(),
                context.getIteration(),
                assistantCount,
                toolCount,
                context.getWorkingMessages().size(),
                hasToolMessages
        );

        if (!hasToolMessages) {
            return ChatStepKey.BUILD_RESULT;
        }

        context.incrementIteration();

        if (context.getIteration() >= context.getMaxIterations()) {
            log.warn(
                    "Chat max iterations reached. executionId={}, iteration={}, maxIterations={}",
                    context.getExecutionId(),
                    context.getIteration(),
                    context.getMaxIterations()
            );
            context.setTerminationReason(
                    AgentTerminationReason.MAX_ITERATIONS_REACHED
            );
            return ChatStepKey.BUILD_RESULT;
        }

        return ChatStepKey.BEGIN;
    }
}