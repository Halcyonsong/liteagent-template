package io.github.halcyonsong.liteagent.provider.openai.agent.chat;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

import java.util.Objects;

/**
 * OpenAI-compatible 同步 chat agent 门面，在通用 chat 执行器之上提供面向 OpenAI-compatible provider 的调用入口。
 */
public class OpenAiChatAgent {

    private final ChatAgent chatAgent;

    public OpenAiChatAgent(ChatAgent chatAgent) {
        this.chatAgent = Objects.requireNonNull(chatAgent, "chatAgent must not be null");
    }

    /**
     * 执行一次 OpenAI-compatible 同步 chat 编排，并返回最终 provider 响应。
     */
    public ChatAgentContext executeContext(Invocation invocation) {
        return chatAgent.executeContext(invocation);
    }

    /**
     * 执行一次 OpenAI chatAgent 编排，并返回最终的 provider 响应。
     */
    public OpenAiChatCompletionResponse execute(Invocation invocation) {
        return (OpenAiChatCompletionResponse) chatAgent.execute(invocation);
    }
}