package io.github.halcyonsong.liteagent.agent.chat.step;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;

/**
 * 同步 chat 编排步骤。
 * <p>
 * 每个步骤只负责当前节点的一段处理逻辑，
 * 并通过返回下一步标识驱动执行器继续推进流程。
 */
@FunctionalInterface
public interface ChatStep {

    /**
     * 执行当前步骤，并返回下一步。
     *
     * @param context 本次调用上下文
     * @return 下一步步骤标识；返回 END 表示流程结束
     */
    ChatStepKey invoke(ChatAgentContext context);
}