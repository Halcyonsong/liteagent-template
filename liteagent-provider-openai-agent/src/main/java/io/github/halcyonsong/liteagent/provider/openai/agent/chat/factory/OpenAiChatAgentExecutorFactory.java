package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.OpenAiChatEnhanceResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.OpenAiChatMapResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatSendRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.OpenAiChatAnalyzeResponseStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.OpenAiChatBuildResultStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible 同步 chat 执行器装配工厂。
 * <p>
 * 负责将 OpenAI provider 已有的 request mapper、advisor、transport、
 * response mapper 组合为一套可由 ChatAgentExecutor 调度的同步 chat 步骤链。
 */
public class OpenAiChatAgentExecutorFactory {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiClientSupport clientSupport;
    private final OpenAiChatTransport chatTransport;
    private final OpenAiChatResponseMapper responseMapper;

    public OpenAiChatAgentExecutorFactory(OpenAiChatRequestMapper requestMapper,
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
    public ChatAgentExecutor create() {
        return create(List.of(), 1000);
    }

    /**
     * 创建一套完整的 OpenAI-compatible 同步 chat 执行器。
     *
     * @param hooks 步骤生命周期钩子
     * @param maxStepCount 单次执行允许的最大步骤数
     * @return 已完成步骤注册的同步 chat 执行器
     */
    public ChatAgentExecutor create(List<StepHook> hooks, int maxStepCount) {
        Map<ChatStepKey, ChatStep> steps = new EnumMap<>(ChatStepKey.class);

        steps.put(ChatStepKey.BEGIN, new OpenAiChatBeginStep());
        steps.put(ChatStepKey.MAP_REQUEST, new OpenAiChatMapRequestStep(requestMapper));
        steps.put(ChatStepKey.ENHANCE_REQUEST, new OpenAiChatEnhanceRequestStep(clientSupport));
        steps.put(ChatStepKey.SEND_REQUEST, new OpenAiChatSendRequestStep(chatTransport));
        steps.put(ChatStepKey.MAP_RESPONSE, new OpenAiChatMapResponseStep(responseMapper));
        steps.put(ChatStepKey.ENHANCE_RESPONSE, new OpenAiChatEnhanceResponseStep(clientSupport));
        steps.put(ChatStepKey.ANALYZE_RESPONSE, new OpenAiChatAnalyzeResponseStep());
        steps.put(ChatStepKey.BUILD_RESULT, new OpenAiChatBuildResultStep());
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);

        return new ChatAgentExecutor(steps, hooks, maxStepCount);
    }
}