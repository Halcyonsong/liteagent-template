package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.Objects;

/**
 * 将统一 Invocation 映射为 OpenAI provider request 和 raw request。
 * <p>
 * 该步骤会将中间产物写入 ChatAgentContext.attributes，供后续步骤复用。
 */
public class OpenAiChatMapRequestStep implements ChatStep {

    private final OpenAiChatRequestMapper requestMapper;

    public OpenAiChatMapRequestStep(OpenAiChatRequestMapper requestMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
    }

    /**
     * 构造 OpenAI provider request，并进一步映射为 raw request。
     * <p>
     * 写入的 attributes：
     * <ul>
     *     <li>openai.provider.request</li>
     *     <li>openai.raw.request</li>
     * </ul>
     */
    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Invocation invocation = context.getInvocation();

        OpenAiChatCompletionRequest providerRequest;
        if (invocation instanceof OpenAiChatCompletionRequest openAiRequest) {
            providerRequest = openAiRequest;
        } else {
            providerRequest = OpenAiChatCompletionRequest.builder()
                    .baseRequest(invocation.getBaseRequest())
                    .chatRequest(invocation.getChatRequest())
                    .completionOptions(null)
                    .build();
        }

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(providerRequest);

        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_REQUEST, providerRequest);
        context.setAttribute(OpenAiChatAgentAttributes.RAW_REQUEST, rawRequest);

        return ChatStepKey.ENHANCE_REQUEST;
    }
}