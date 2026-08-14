package io.github.halcyonsong.liteagent.provider.openai.agent.step.response;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

/**
 * 分析一轮 OpenAI 响应。
 * <p>
 * 当前最小实现尚未接入 tool 自动执行，
 * 因此只校验响应是否存在，并决定是否进入结果构建阶段。
 */
public class OpenAiAnalyzeResponseStep implements AgentStep {

    /**
     * 分析当前 provider response。
     * <p>
     * 当前实现：
     * <ul>
     *     <li>响应为空：标记模型错误并结束</li>
     *     <li>响应存在：进入 BUILD_RESULT</li>
     * </ul>
     */
    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return AgentStepKey.END;
        }

        return AgentStepKey.BUILD_RESULT;
    }
}