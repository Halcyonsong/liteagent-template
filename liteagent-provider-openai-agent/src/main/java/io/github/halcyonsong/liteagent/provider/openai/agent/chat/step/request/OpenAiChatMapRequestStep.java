package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestBuildSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;

import java.util.Objects;

/**
 * 将统一 Invocation 映射为 OpenAI provider request 和 raw request。
 */
public class OpenAiChatMapRequestStep implements ChatStep {

    private final OpenAiChatRequestMapper requestMapper;

    public OpenAiChatMapRequestStep(OpenAiChatRequestMapper requestMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
    }
    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiAgentRequestBuildSupport.RequestBuildResult result = OpenAiAgentRequestBuildSupport.buildRequest(
                context.getInvocation(),
                context.getWorkingMessages(),
                requestMapper
        );

        context.setAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST, result.providerRequest());
        context.setAttribute(OpenAiAgentAttributes.RAW_REQUEST, result.rawRequest());

        return ChatStepKey.ENHANCE_REQUEST;
    }
}