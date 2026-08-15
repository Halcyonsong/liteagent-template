package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;

public class ToolRegistrySupport {

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
