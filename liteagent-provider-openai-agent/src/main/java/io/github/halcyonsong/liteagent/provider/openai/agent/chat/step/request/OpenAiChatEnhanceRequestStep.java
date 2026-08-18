package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestSupport;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 对 OpenAI raw request 应用增强逻辑，执行 request advisor 并设定为普通 chat 请求。
 */
@Slf4j
public class OpenAiChatEnhanceRequestStep implements ChatStep {

    private final OpenAiAdvisorsExecutor clientSupport;

    public OpenAiChatEnhanceRequestStep(OpenAiAdvisorsExecutor clientSupport) {
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionRequest providerRequest =
                OpenAiAgentRequestSupport.requireProviderRequest(context);
        OpenAiChatCompletionRawRequest rawRequest =
                OpenAiAgentRequestSupport.requireRawRequest(context);

        clientSupport.applyRequestAdvisors(providerRequest, rawRequest);
        rawRequest.setStream(false);

        log.debug(
                "Enhanced raw request. execId={}, iter={}, advisors={}, tools={}, toolChoice={}, stream={}",
                context.getExecutionId(),
                context.getIteration(),
                providerRequest.getRequestAdvisors().size(),
                rawRequest.getTools() == null ? 0 : rawRequest.getTools().size(),
                rawRequest.getToolChoice() != null,
                rawRequest.getStream()
        );

        return ChatStepKey.SEND_REQUEST;
    }
}