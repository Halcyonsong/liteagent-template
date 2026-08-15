package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

import java.util.Objects;

/**
 * 初始化 chat 编排内部的工作态消息历史。
 * <p>
 * 当前最小实现将 invocation 中的原始消息复制到 workingMessages。
 * 该步骤本身不负责轮次判断，是否执行由上游步骤决定。
 */
public class OpenAiChatInitWorkingMessagesStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Invocation invocation = context.getInvocation();
        context.clearWorkingMessages();
        context.appendWorkingMessages(invocation.getChatRequest().getMessages());

        return ChatStepKey.MAP_REQUEST;
    }
}