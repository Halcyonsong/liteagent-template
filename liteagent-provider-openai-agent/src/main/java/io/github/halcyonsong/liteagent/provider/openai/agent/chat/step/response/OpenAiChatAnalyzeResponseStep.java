package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

/**
 * 分析一轮 OpenAI 响应。
 * <p>
 * 当前最小实现尚未接入 tool 自动执行，
 * 但 provider response 内部的 assistant 消息已经会保留
 * reasoningContent 与 toolCalls 等结构化信息，
 * 后续可在此节点中继续解析并决定是否进入工具执行分支。
 */
public class OpenAiChatAnalyzeResponseStep implements ChatStep {

    /**
     * 分析当前 provider response。
     * <p>
     * 当前实现：
     * 1. 响应为空：标记模型错误并结束
     * 2. 响应存在：进入 BUILD_RESULT
     * <p>
     * 后续可在这里读取首条 AssistantResponseMessage，
     * 根据 toolCalls 是否为空决定进入 BUILD_RESULT 还是 EXECUTE_TOOL。
     */
    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        return ChatStepKey.BUILD_RESULT;
    }
}