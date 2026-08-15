package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

/**
 * OpenAI agent 编排起始步骤。
 * <p>
 * 第 0 轮先初始化工作态消息历史；
 * 后续轮次直接进入请求映射阶段，复用已有 workingMessages。
 */
public class OpenAiChatBeginStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        if (context.getIteration() == 0) {
            return ChatStepKey.INIT_WORKING_MESSAGES;
        }
        return ChatStepKey.MAP_REQUEST;
    }
}