package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

/**
 * OpenAI agent 编排起始步骤。
 * <p>
 * 当前最小实现默认进入普通 chat 请求链路。
 */
public class OpenAiChatBeginStep implements ChatStep {

    /**
     * 进入 OpenAI 请求映射阶段。
     */
    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        return ChatStepKey.MAP_REQUEST;
    }
}