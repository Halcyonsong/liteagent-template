package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;

/**
 * 从 OpenAI request advisors 中提取 ToolRegistry，当前只识别 OpenAiRegistryToolsAdvisor。
 */
public class ToolRegistrySupport {

    /**
     * 从 invocation 中解析工具注册表，不存在时返回 null。
     */
    public static ToolRegistry resolveToolRegistry(Invocation invocation) {
        if (!(invocation instanceof OpenAiChatCompletionRequest request)) {
            return null;
        }

        for (RequestAdvisor<?, ?> advisor : request.getRequestAdvisors()) {
            if (advisor instanceof OpenAiRegistryToolsAdvisor toolsAdvisor) {
                return toolsAdvisor.getRegistry();
            }
        }
        return null;
    }

}
