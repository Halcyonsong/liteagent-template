package io.github.halcyonsong.liteagent.provider.openai.agent;

import io.github.halcyonsong.liteagent.agent.Agent;
import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

import java.util.Objects;

/**
 * OpenAI provider 的最小 agent 门面。
 * <p>
 * 该类在通用 agent 执行器之上，提供面向 OpenAI-compatible provider 的调用入口。
 * 当前最小实现只支持同步 chat 编排，并直接返回 provider 响应对象。
 */
public class OpenAiAgent {

    private final Agent agent;

    public OpenAiAgent(Agent agent) {
        this.agent = Objects.requireNonNull(agent, "agent must not be null");
    }

    /**
     * 执行一次 OpenAI agent 编排，并返回完整上下文。
     */
    public AgentContext executeContext(Invocation invocation) {
        return agent.executeContext(invocation);
    }

    /**
     * 执行一次 OpenAI agent 编排，并返回最终的 provider 响应。
     */
    public OpenAiChatCompletionResponse execute(Invocation invocation) {
        return (OpenAiChatCompletionResponse) agent.execute(invocation);
    }
}