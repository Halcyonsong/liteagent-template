package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 初始化 chat 编排内部的工作态消息历史。
 */
@Slf4j
public class OpenAiChatInitWorkingMessagesStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        Invocation invocation = context.getInvocation();
        context.clearWorkingMessages();
        context.appendWorkingMessages(invocation.getChatRequest().getMessages());

        log.debug(
                "Init working messages. execId={}, msgs={}",
                context.getExecutionId(),
                context.getWorkingMessages().size()
        );

        return ChatStepKey.INIT_TOOL_REGISTRY;
    }
}