package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatInitToolRegistryStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatInitWorkingMessagesStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request.OpenAiChatSendRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response.*;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI chat 编排执行器工厂，组装 chat 主链路所需的 step 并生成可执行的 ChatAgentExecutor。
 */
public class OpenAiChatAgentExecutorFactory {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiAdvisorsExecutor advisorsExecutor;
    private final OpenAiChatTransport chatTransport;
    private final OpenAiChatResponseMapper responseMapper;
    private final ToolExecutor toolExecutor;

    public OpenAiChatAgentExecutorFactory(
            OpenAiChatRequestMapper requestMapper,
            OpenAiAdvisorsExecutor advisorsExecutor,
            OpenAiChatTransport chatTransport,
            OpenAiChatResponseMapper responseMapper
    ) {
        this(requestMapper, advisorsExecutor, chatTransport, responseMapper, new ReflectionToolExecutor());
    }

    public OpenAiChatAgentExecutorFactory(OpenAiChatRequestMapper requestMapper,
                                          OpenAiAdvisorsExecutor advisorsExecutor,
                                          OpenAiChatTransport chatTransport,
                                          OpenAiChatResponseMapper responseMapper,
                                          ToolExecutor toolExecutor) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.advisorsExecutor = Objects.requireNonNull(advisorsExecutor, "advisorsExecutor must not be null");
        this.chatTransport = Objects.requireNonNull(chatTransport, "chatTransport must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
    }

    public ChatAgentExecutor create() {
        return create(List.of(), 1000, 10);
    }

    /**
     * 组装 OpenAI chat 步骤注册表并创建执行器。
     */
    public ChatAgentExecutor create(List<StepHook> hooks, int maxStepCount) {
        return create(hooks, maxStepCount, 10);
    }

    /**
     * 组装 OpenAI chat 步骤注册表并创建执行器，允许指定最大迭代轮次。
     */
    public ChatAgentExecutor create(List<StepHook> hooks, int maxStepCount, int maxIterations) {
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();

        steps.put(ChatStepKey.BEGIN, new OpenAiChatBeginStep());
        steps.put(ChatStepKey.INIT_WORKING_MESSAGES, new OpenAiChatInitWorkingMessagesStep());
        steps.put(ChatStepKey.INIT_TOOL_REGISTRY, new OpenAiChatInitToolRegistryStep());
        steps.put(ChatStepKey.MAP_REQUEST, new OpenAiChatMapRequestStep(requestMapper));
        steps.put(ChatStepKey.ENHANCE_REQUEST, new OpenAiChatEnhanceRequestStep(advisorsExecutor));
        steps.put(ChatStepKey.SEND_REQUEST, new OpenAiChatSendRequestStep(chatTransport));
        steps.put(ChatStepKey.MAP_RESPONSE, new OpenAiChatMapResponseStep(responseMapper));
        steps.put(ChatStepKey.ENHANCE_RESPONSE, new OpenAiChatEnhanceResponseStep(advisorsExecutor));
        steps.put(ChatStepKey.ANALYZE_RESPONSE, new OpenAiChatAnalyzeResponseStep());
        steps.put(ChatStepKey.EXECUTE_TOOL, new OpenAiChatExecuteToolStep(toolExecutor));
        steps.put(ChatStepKey.APPEND_MESSAGES, new OpenAiChatAppendMessagesStep());
        steps.put(ChatStepKey.BUILD_RESULT, new OpenAiChatBuildResultStep());
        steps.put(ChatStepKey.END, new OpenAiChatEndStep());

        return new ChatAgentExecutor(steps, hooks, maxStepCount, maxIterations);
    }
}