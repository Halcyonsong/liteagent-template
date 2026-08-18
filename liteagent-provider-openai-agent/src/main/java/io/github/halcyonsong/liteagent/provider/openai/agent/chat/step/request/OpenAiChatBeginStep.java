package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI agent 编排起始步骤。
 */
@Slf4j
public class OpenAiChatBeginStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        if (context.getIteration() == 0) {
            if (context.getWorkingMessages().isEmpty()) {
                log.debug("workingMessages empty, init");
                return ChatStepKey.INIT_WORKING_MESSAGES;
            }
            if (context.getToolRegistry() == null) {
                log.debug("toolRegistry null, init");
                return ChatStepKey.INIT_TOOL_REGISTRY;
            }
        }
        log.debug("map request");
        return ChatStepKey.MAP_REQUEST;
    }
}