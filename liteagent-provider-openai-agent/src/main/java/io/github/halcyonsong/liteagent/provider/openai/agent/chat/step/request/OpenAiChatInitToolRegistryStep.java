package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.ToolRegistrySupport;
import lombok.extern.slf4j.Slf4j;

/**
 * 初始化 chat 编排中的工具注册表，仅在第 0 轮通过扫描 request advisors 提取 ToolRegistry。
 */
@Slf4j
public class OpenAiChatInitToolRegistryStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        ToolRegistry registry = ToolRegistrySupport.resolveToolRegistry(context.getInvocation());
        context.setToolRegistry(registry);

        log.debug(
                "Init tool registry. execId={}, iter={}, resolved={}",
                context.getExecutionId(),
                context.getIteration(),
                registry != null
        );

        return ChatStepKey.MAP_REQUEST;
    }

}