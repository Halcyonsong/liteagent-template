package io.github.halcyonsong.liteagent.agent.chat.step;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;

/**
 * 同步 chat 编排步骤，通过返回下一步标识驱动执行器推进流程。
 */
@FunctionalInterface
public interface ChatStep {

    /** @return 下一步步骤标识；返回 END 表示流程结束 */
    ChatStepKey invoke(ChatAgentContext context);
}