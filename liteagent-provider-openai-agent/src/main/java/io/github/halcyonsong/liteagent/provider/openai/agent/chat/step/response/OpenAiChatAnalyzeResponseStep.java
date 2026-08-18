package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 分析当前轮 chat 响应，决定进入普通收尾还是工具执行分支。assistant 消息先写入 pending 区，由 APPEND_MESSAGES 落入 workingMessages。
 */
@Slf4j
public class OpenAiChatAnalyzeResponseStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            log.warn(
                    "Missing provider response. execId={}, iter={}",
                    context.getExecutionId(),
                    context.getIteration()
            );
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        List<AssistantResponseMessage> assistantMessages = OpenAiToolCallSupport.collectAssistantMessages(response);
        boolean hasToolCalls = OpenAiToolCallSupport.hasAnyToolCalls(response);

        log.debug(
                "Analyzed response. execId={}, iter={}, msgs={}, tools={}, next={}",
                context.getExecutionId(),
                context.getIteration(),
                assistantMessages.size(),
                hasToolCalls,
                hasToolCalls ? "EXECUTE_TOOL" : "APPEND_MESSAGES"
        );

        if (!hasToolCalls) {
            context.clearPendingMessages();
            context.appendPendingAssistantMessages(assistantMessages);
            return ChatStepKey.APPEND_MESSAGES;
        }

        return ChatStepKey.EXECUTE_TOOL;
    }
}