package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

/**
 * 构建最终结果步骤。
 * <p>
 * 当前最小实现直接将 OpenAI provider response 作为最终结果写入 ChatAgentContext。
 */
public class OpenAiChatBuildResultStep implements ChatStep {

    /**
     * 将 provider response 写入最终结果，并标记本次执行正常完成。
     */
    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            throw new IllegalStateException("Missing provider response in agent context");
        }

        context.setResult(response);
        context.setTerminationReason(AgentTerminationReason.COMPLETED);
        return ChatStepKey.END;
    }
}