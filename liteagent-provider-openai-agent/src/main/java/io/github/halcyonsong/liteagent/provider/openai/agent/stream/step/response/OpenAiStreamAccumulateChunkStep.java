package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.state.OpenAiStreamRoundAccumulator;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamRoundSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

/**
 * 对单轮流式 chunk 做最小聚合。
 */
public class OpenAiStreamAccumulateChunkStep implements StreamStep<Flux<OpenAiStreamCompletionResponse>> {

    @Override
    public StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> apply(
            Flux<OpenAiStreamCompletionResponse> upstream,
            StreamAgentContext<?> context
    ) {
        OpenAiStreamRoundAccumulator accumulator = OpenAiStreamRoundSupport.getOrCreateAccumulator(context);

        Flux<OpenAiStreamCompletionResponse> stream = upstream.doOnNext(accumulator::accumulate);

        return new StreamApplyResult<>(stream, StreamStepKey.ANALYZE_CHUNK);
    }
}