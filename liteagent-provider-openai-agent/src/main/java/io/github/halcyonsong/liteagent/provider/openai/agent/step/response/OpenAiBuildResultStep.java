package io.github.halcyonsong.liteagent.provider.openai.agent.step.response;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

/**
 * 构建最终结果步骤。
 * <p>
 * 当前最小实现直接将 OpenAI provider response 作为最终结果写入 AgentContext。
 */
public class OpenAiBuildResultStep implements AgentStep {

    /**
     * 将 provider response 写入最终结果，并标记本次执行正常完成。
     */
    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            throw new IllegalStateException("Missing provider response in agent context");
        }

        context.setResult(response);
        context.setTerminationReason(AgentTerminationReason.COMPLETED);
        return AgentStepKey.END;
    }
}