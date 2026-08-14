package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.constant.OpenAiStreamAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;

import java.util.List;
import java.util.Objects;

/**
 * 将 workingMessages 映射为 OpenAI provider 请求。
 * <p>
 * 该步骤在每一轮都会执行一次，
 * 并始终基于当前 workingMessages 重建请求，而不是复用上一轮 raw request。
 */
public class OpenAiStreamMapRequestStep implements StreamSyncStep {

    private final OpenAiChatRequestMapper requestMapper;

    public OpenAiStreamMapRequestStep(OpenAiChatRequestMapper requestMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
    }

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        if (context.getWorkingMessages().isEmpty()) {
            throw new IllegalStateException(
                    "workingMessages must be initialized before mapping request"
            );
        }

        Invocation invocation = context.getInvocation();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessages(List.copyOf(context.getWorkingMessages()))
                .build();

        OpenAiChatCompletionRequest providerRequest = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(chatRequest)
                .completionOptions(null)
                .build();

        context.setAttribute(
                OpenAiStreamAgentAttributes.PROVIDER_REQUEST,
                providerRequest
        );

        context.setAttribute(
                OpenAiStreamAgentAttributes.RAW_REQUEST,
                requestMapper.toRawRequest(providerRequest)
        );

        return StreamStepKey.ENHANCE_REQUEST;
    }
}