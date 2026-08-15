package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;

import java.util.Objects;

/**
 * 初始化 chat 编排内部的工作态消息历史。
 */
public class OpenAiChatInitWorkingMessagesStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Objects.requireNonNull(context, "context must not be null");

        Invocation invocation = context.getInvocation();
        context.clearWorkingMessages();
        context.appendWorkingMessages(invocation.getChatRequest().getMessages());

        return ChatStepKey.INIT_TOOL_REGISTRY;
    }
}