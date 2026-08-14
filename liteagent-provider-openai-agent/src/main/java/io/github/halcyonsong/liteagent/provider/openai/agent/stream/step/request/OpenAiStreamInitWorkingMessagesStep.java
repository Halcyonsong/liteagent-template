package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;

/**
 * 初始化 OpenAI 流式编排的工作态消息。
 * <p>
 * 当前只复制原始 Invocation 中的请求消息，
 * 暂不追加历史记忆窗口内容。
 * <p>
 * 后续如需接入历史记忆，可继续在该步骤中控制追加顺序。
 */
public class OpenAiStreamInitWorkingMessagesStep implements StreamSyncStep {

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        context.clearWorkingMessages();
        context.appendWorkingMessages(
                context.getInvocation().getChatRequest().getMessages()
        );
        return StreamStepKey.MAP_REQUEST;
    }
}