package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiAgentRequestBuildSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 基于 workingMessages 构造当前轮的 OpenAI provider request 和 raw request。
 */
@Slf4j
public class OpenAiStreamMapRequestStep implements StreamSyncStep {

    private final OpenAiChatRequestMapper requestMapper;

    public OpenAiStreamMapRequestStep(OpenAiChatRequestMapper requestMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
    }

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        OpenAiAgentRequestBuildSupport.RequestBuildResult result = OpenAiAgentRequestBuildSupport.buildRequest(
                context.getInvocation(),
                context.getWorkingMessages(),
                requestMapper
        );

        context.setAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST, result.providerRequest());
        context.setAttribute(OpenAiAgentAttributes.RAW_REQUEST, result.rawRequest());

        log.debug(
                "Mapped stream request. " +
                        "executionId={}, " +
                        "iteration={}, " +
                        "workingMessageCount={}, " +
                        "rawMessageCount={}, " +
                        "hasCompletionOptions={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getWorkingMessages().size(),
                result.rawRequest().getMessages() == null ? 0 : result.rawRequest().getMessages().size(),
                result.providerRequest().getCompletionOptions() != null
        );

        return StreamStepKey.ENHANCE_REQUEST;
    }
}