package io.github.halcyonsong.liteagent.provider.openai.agent.stream.support;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.state.OpenAiStreamRoundAccumulator;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

/**
 * OpenAI 流式轮次辅助工具。
 */
public final class OpenAiStreamRoundSupport {

    private OpenAiStreamRoundSupport() {
    }

    public static OpenAiStreamRoundAccumulator getOrCreateAccumulator(StreamAgentContext<?> context) {
        StreamRoundState roundState = context.currentRound();
        OpenAiStreamRoundAccumulator accumulator = (OpenAiStreamRoundAccumulator) roundState.getAccumulator();
        if (accumulator == null) {
            accumulator = new OpenAiStreamRoundAccumulator();
            roundState.setAccumulator(accumulator);
        }
        return accumulator;
    }

    public static OpenAiChatCompletionRequest getProviderRequest(StreamAgentContext<?> context, String key) {
        return context.getAttribute(key, OpenAiChatCompletionRequest.class);
    }

    public static OpenAiChatCompletionRawRequest getRawRequest(StreamAgentContext<?> context, String key) {
        return context.getAttribute(key, OpenAiChatCompletionRawRequest.class);
    }
}