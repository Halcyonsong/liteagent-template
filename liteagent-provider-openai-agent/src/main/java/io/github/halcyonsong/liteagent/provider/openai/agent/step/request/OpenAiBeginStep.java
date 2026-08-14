package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;

/**
 * OpenAI agent 编排起始步骤。
 * <p>
 * 当前最小实现默认进入普通 chat 请求链路。
 */
public class OpenAiBeginStep implements AgentStep {

    /**
     * 进入 OpenAI 请求映射阶段。
     */
    @Override
    public AgentStepKey invoke(AgentContext context) {
        return AgentStepKey.MAP_REQUEST;
    }
}