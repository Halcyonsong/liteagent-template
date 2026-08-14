package io.github.halcyonsong.liteagent.provider.openai.agent.factory;

import io.github.halcyonsong.liteagent.agent.executor.AgentExecutor;
import io.github.halcyonsong.liteagent.agent.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.response.OpenAiEnhanceResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.response.OpenAiMapChatResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.request.OpenAiSendChatRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.response.OpenAiAnalyzeResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.request.OpenAiBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.response.OpenAiBuildResultStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.request.OpenAiEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.step.request.OpenAiMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI agent 执行器装配工厂。
 * <p>
 * 负责将 OpenAI provider 现有的 mapper、advisor、transport、response mapper
 * 组合为一套可由 AgentExecutor 调度的同步步骤链。
 */
public class OpenAiAgentExecutorFactory {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiClientSupport clientSupport;
    private final OpenAiChatTransport chatTransport;
    private final OpenAiChatResponseMapper responseMapper;

    public OpenAiAgentExecutorFactory(OpenAiChatRequestMapper requestMapper,
                                      OpenAiClientSupport clientSupport,
                                      OpenAiChatTransport chatTransport,
                                      OpenAiChatResponseMapper responseMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
        this.chatTransport = Objects.requireNonNull(chatTransport, "chatTransport must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
    }

    /**
     * 使用默认 hook 和默认最大步骤数创建执行器。
     */
    public AgentExecutor create() {
        return create(List.of(), 1000);
    }

    /**
     * 创建一套完整的 OpenAI 同步 agent 执行器。
     *
     * @param hooks 步骤生命周期钩子
     * @param maxStepCount 单次执行允许的最大步骤数
     * @return 已完成步骤注册的执行器
     */
    public AgentExecutor create(List<StepHook> hooks, int maxStepCount) {
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);

        steps.put(AgentStepKey.BEGIN, new OpenAiBeginStep());
        steps.put(AgentStepKey.MAP_REQUEST, new OpenAiMapRequestStep(requestMapper));
        steps.put(AgentStepKey.ENHANCE_REQUEST, new OpenAiEnhanceRequestStep(clientSupport));
        steps.put(AgentStepKey.SEND_CHAT_REQUEST, new OpenAiSendChatRequestStep(chatTransport));
        steps.put(AgentStepKey.MAP_CHAT_RESPONSE, new OpenAiMapChatResponseStep(responseMapper));
        steps.put(AgentStepKey.ENHANCE_RESPONSE, new OpenAiEnhanceResponseStep(clientSupport));
        steps.put(AgentStepKey.ANALYZE_RESPONSE, new OpenAiAnalyzeResponseStep());
        steps.put(AgentStepKey.BUILD_RESULT, new OpenAiBuildResultStep());
        steps.put(AgentStepKey.END, context -> AgentStepKey.END);

        return new AgentExecutor(steps, hooks, maxStepCount);
    }
}