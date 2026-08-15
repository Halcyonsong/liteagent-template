package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatInitToolRegistryStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatInitWorkingMessagesStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatSendRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.*;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public ChatAgentExecutor create() {
        return create(List.of(), 1000);
    }

    public ChatAgentExecutor create(List<StepHook> hooks, int maxStepCount) {
        Map<ChatStepKey, ChatStep> steps = new EnumMap<>(ChatStepKey.class);

        steps.put(ChatStepKey.BEGIN, new OpenAiChatBeginStep());
        steps.put(ChatStepKey.INIT_WORKING_MESSAGES, new OpenAiChatInitWorkingMessagesStep());
        steps.put(ChatStepKey.INIT_TOOL_REGISTRY, new OpenAiChatInitToolRegistryStep());
        steps.put(ChatStepKey.MAP_REQUEST, new OpenAiChatMapRequestStep(requestMapper));
        steps.put(ChatStepKey.ENHANCE_REQUEST, new OpenAiChatEnhanceRequestStep(clientSupport));
        steps.put(ChatStepKey.SEND_REQUEST, new OpenAiChatSendRequestStep(chatTransport));
        steps.put(ChatStepKey.MAP_RESPONSE, new OpenAiChatMapResponseStep(responseMapper));
        steps.put(ChatStepKey.ENHANCE_RESPONSE, new OpenAiChatEnhanceResponseStep(clientSupport));
        steps.put(ChatStepKey.ANALYZE_RESPONSE, new OpenAiChatAnalyzeResponseStep());
        steps.put(ChatStepKey.EXECUTE_TOOL, new OpenAiChatExecuteToolStep());
        steps.put(ChatStepKey.APPEND_MESSAGES, new OpenAiChatAppendMessagesStep());
        steps.put(ChatStepKey.BUILD_RESULT, new OpenAiChatBuildResultStep());
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);

        return new ChatAgentExecutor(steps, hooks, maxStepCount);
    }
}