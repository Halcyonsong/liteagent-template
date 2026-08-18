package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.ToolRegistrySupport;
import lombok.extern.slf4j.Slf4j;

/**
 * 初始化流式编排中的工具注册表，仅在第 0 轮通过扫描 request advisors 提取 ToolRegistry。
 */
@Slf4j
public class OpenAiStreamInitToolRegistryStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        ToolRegistry registry = ToolRegistrySupport.resolveToolRegistry(context.getInvocation());
        context.setToolRegistry(registry);

        log.debug(
                "Init stream tool registry. execId={}, resolved={}",
                context.getExecutionId(),
                registry != null
        );

        return StreamStepKey.MAP_REQUEST;
    }

}