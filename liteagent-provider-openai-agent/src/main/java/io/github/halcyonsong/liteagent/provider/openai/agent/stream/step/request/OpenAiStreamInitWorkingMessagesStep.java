package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * 初始化 OpenAI 流式编排的工作态消息。
 */
public class OpenAiStreamInitWorkingMessagesStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        context.clearWorkingMessages();
        context.appendWorkingMessages(
                context.getInvocation().getChatRequest().getMessages()
        );
        return StreamStepKey.INIT_TOOL_REGISTRY;
    }
}