package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.Objects;

/**
 * 对 OpenAI raw request 应用增强逻辑。
 * <p>
 * 当前主要负责执行 request advisor，并显式设定为普通 chat 请求。
 */
public class OpenAiEnhanceRequestStep implements AgentStep {

    private final OpenAiClientSupport clientSupport;

    public OpenAiEnhanceRequestStep(OpenAiClientSupport clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    /**
     * 从上下文中读取 provider request 和 raw request，
     * 应用 advisor 后推进到普通请求执行阶段。
     */
    @Override
    public AgentStepKey invoke(AgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );

        if (providerRequest == null) {
            throw new IllegalStateException("Missing provider request in agent context");
        }
        if (rawRequest == null) {
            throw new IllegalStateException("Missing raw request in agent context");
        }

        clientSupport.applyRequestAdvisors(providerRequest, rawRequest);
        rawRequest.setStream(false);

        return AgentStepKey.SEND_CHAT_REQUEST;
    }
}