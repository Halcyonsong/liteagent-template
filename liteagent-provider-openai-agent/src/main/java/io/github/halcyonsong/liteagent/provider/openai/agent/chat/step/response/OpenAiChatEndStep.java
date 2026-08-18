package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Chat 编排终止步骤，可用于收尾日志、metrics 上报或资源清理。
 */
@Slf4j
public class OpenAiChatEndStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        log.debug(
                "Chat agent finished. execId={}, iter={}, reason={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getTerminationReason()
        );
        return ChatStepKey.END;
    }
}