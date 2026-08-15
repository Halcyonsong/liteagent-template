package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

import java.util.Objects;

/**
 * 分析当前轮 OpenAI 响应。
 *
 * <p>无论响应中是否存在工具调用，都会将 assistant 响应暂存到
 * pendingAssistantMessages。后续由 APPEND_MESSAGES 统一写入 workingMessages。</p>
 */
public class OpenAiChatAnalyzeResponseStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        /*
         * 普通响应也需要写入 workingMessages，供后续记忆窗口或日志增强器使用。
         *
         * 工具调用响应不会在这里写入，交给 EXECUTE_TOOL 统一处理，
         * 避免 assistant 消息被重复追加。
         */
        if (!OpenAiToolCallSupport.hasAnyToolCalls(response)) {
            context.clearPendingMessages();
            context.appendPendingAssistantMessages(
                    OpenAiToolCallSupport.collectAssistantMessages(response)
            );
            return ChatStepKey.APPEND_MESSAGES;
        }

        return ChatStepKey.EXECUTE_TOOL;
    }
}