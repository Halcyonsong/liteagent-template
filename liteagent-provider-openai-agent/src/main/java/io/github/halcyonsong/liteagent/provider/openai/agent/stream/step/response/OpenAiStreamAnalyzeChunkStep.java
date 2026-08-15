package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.state.OpenAiStreamRoundAccumulator;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamRoundSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

/**
 * 分析当前轮流式输出。
 * <p>
 * 当前最小实现负责：
 * 1. 在检测到终止 chunk 时标记本轮完成
 * 2. 将聚合结果写入 finalResponse
 * 3. 预留下一步决策节点，不在这里直接决定是否执行工具
 */
public class OpenAiStreamAnalyzeChunkStep implements StreamStep<Flux<OpenAiStreamCompletionResponse>> {

    @Override
    public StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> apply(
            Flux<OpenAiStreamCompletionResponse> upstream,
            StreamAgentContext<?> context
    ) {
        StreamRoundState roundState = context.currentRound();
        OpenAiStreamRoundAccumulator accumulator =
                OpenAiStreamRoundSupport.getOrCreateAccumulator(context);

        Flux<OpenAiStreamCompletionResponse> stream = upstream
                .doOnNext(chunk -> {
                    if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                        return;
                    }

                    chunk.getChoices().forEach(choice -> {
                        // 这里依赖 stream mapper 保留 finishReason 的 null 语义；
                        // 中间 chunk 的 null 不能被映射成 UNKNOWN，否则会误判为本轮完成。
                        if (choice.getFinishReason() != null) {
                            roundState.setRoundComplete(true);
                            roundState.setFinalResponse(accumulator.toFinalResponse());
                        }
                    });
                })
                .doOnComplete(() -> {
                    // 兜底：某些 provider 可能没有显式 finishReason，但流已经完成
                    if (!roundState.isRoundComplete()) {
                        roundState.setRoundComplete(true);
                        roundState.setFinalResponse(accumulator.toFinalResponse());
                    }
                });

        return new StreamApplyResult<>(stream, StreamStepKey.STREAM_END);
    }
}