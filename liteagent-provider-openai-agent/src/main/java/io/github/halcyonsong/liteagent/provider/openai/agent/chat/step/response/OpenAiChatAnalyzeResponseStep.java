package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

/**
 * 分析一轮 OpenAI 响应。
 */
public class OpenAiChatAnalyzeResponseStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        return ChatStepKey.BUILD_RESULT;
    }
}