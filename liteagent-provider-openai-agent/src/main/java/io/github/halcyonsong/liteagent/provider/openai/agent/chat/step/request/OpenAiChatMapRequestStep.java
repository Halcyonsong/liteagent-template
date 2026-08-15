package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.List;
import java.util.Objects;

/**
 * 将统一 Invocation 映射为 OpenAI provider request 和 raw request。
 * <p>
 * 该步骤会优先使用 ChatAgentContext 中的 workingMessages 重建请求，
 * 从而支持多轮工具调用时持续追加 assistant/tool 消息。
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
        List<Message> workingMessages = context.getWorkingMessages();

        if (workingMessages.isEmpty()) {
            throw new IllegalStateException(
                    "Working messages must not be empty when mapping chat request"
            );
        }

        ChatRequest chatRequest = ChatRequest.builder().addMessages(workingMessages).build();

        OpenAiChatCompletionRequest providerRequest;
        if (invocation instanceof OpenAiChatCompletionRequest openAiRequest) {
            providerRequest = OpenAiChatCompletionRequest.builder()
                    .baseRequest(openAiRequest.getBaseRequest())
                    .chatRequest(chatRequest)
                    .completionOptions(openAiRequest.getCompletionOptions())
                    .requestAdvisors(openAiRequest.getRequestAdvisors())
                    .chatResponseAdvisors(openAiRequest.getChatResponseAdvisors())
                    .streamResponseAdvisors(openAiRequest.getStreamResponseAdvisors())
                    .build();
        } else {
            providerRequest = OpenAiChatCompletionRequest.builder()
                    .baseRequest(invocation.getBaseRequest())
                    .chatRequest(chatRequest)
                    .completionOptions(null)
                    .build();
        }

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(providerRequest);

        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_REQUEST, providerRequest);
        context.setAttribute(OpenAiChatAgentAttributes.RAW_REQUEST, rawRequest);

        return ChatStepKey.ENHANCE_REQUEST;
    }
}