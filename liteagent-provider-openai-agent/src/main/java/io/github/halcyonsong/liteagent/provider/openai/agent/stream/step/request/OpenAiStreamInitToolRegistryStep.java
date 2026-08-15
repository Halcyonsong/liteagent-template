package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.ToolRegistrySupport;

/**
 * 初始化流式编排中的工具注册表。
 * <p>
 * 只在第 0 轮执行，通过扫描 request advisors 提取
 * OpenAiRegistryToolsAdvisor 中持有的 ToolRegistry。
 */
public class OpenAiStreamInitToolRegistryStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        ToolRegistry registry = ToolRegistrySupport.resolveToolRegistry(context.getInvocation());
        context.setToolRegistry(registry);
        return StreamStepKey.MAP_REQUEST;
    }

}