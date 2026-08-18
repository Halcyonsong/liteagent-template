package io.github.halcyonsong.liteagent.provider.openai.agent.stream;

import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * OpenAI-compatible 流式 agent 门面，在通用 stream 执行器之上提供面向 OpenAI-compatible provider 的流式调用入口。
 */
public class OpenAiStreamAgent {

    private final StreamAgent<OpenAiStreamCompletionResponse> streamAgent;

    public OpenAiStreamAgent(StreamAgent<OpenAiStreamCompletionResponse> streamAgent) {
        this.streamAgent = Objects.requireNonNull(streamAgent, "streamAgent must not be null");
    }

    /**
     * 执行一次 OpenAI-compatible 流式编排，并返回完整上下文。
     */
    public StreamAgentContext<OpenAiStreamCompletionResponse> executeContext(Invocation invocation) {
        return streamAgent.executeContext(invocation);
    }

    /**
     * 执行一次 OpenAI-compatible 流式编排，并返回 provider 对外输出流。
     */
    public Flux<OpenAiStreamCompletionResponse> execute(Invocation invocation) {
        return streamAgent.execute(invocation);
    }
}