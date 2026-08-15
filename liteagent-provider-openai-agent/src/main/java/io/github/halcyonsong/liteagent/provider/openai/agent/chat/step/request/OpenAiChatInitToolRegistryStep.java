package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.ToolRegistrySupport;

/**
 * 初始化 chat 编排中的工具注册表。
 * <p>
 * 只在第 0 轮执行，通过扫描 request advisors 提取
 * OpenAiRegistryToolsAdvisor 中持有的 ToolRegistry。
 */
public class OpenAiChatInitToolRegistryStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        ToolRegistry registry = ToolRegistrySupport.resolveToolRegistry(context.getInvocation());
        context.setToolRegistry(registry);
        return ChatStepKey.MAP_REQUEST;
    }

}