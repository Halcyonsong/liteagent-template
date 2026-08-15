package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;

/**
 * OpenAI agent 编排起始步骤。
 */
public class OpenAiChatBeginStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        if (context.getIteration() == 0) {
            if (context.getWorkingMessages().isEmpty()) {
                return ChatStepKey.INIT_WORKING_MESSAGES;
            }
            if (context.getToolRegistry() == null) {
                return ChatStepKey.INIT_TOOL_REGISTRY;
            }
        }
        return ChatStepKey.MAP_REQUEST;
    }
}