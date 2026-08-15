package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.state.OpenAiStreamRoundAccumulator;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamRoundSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分析当前轮流式输出。
 *
 * <p>finishReason 存在时，最后一个正常 chunk 就会触发 expand。
 * 如果供应商没有返回 finishReason，则在流完成后追加一个内部哨兵，
 * 让 expand 获得一次额外的轮次收尾机会。</p>
 */
public class OpenAiStreamAnalyzeChunkStep
        implements StreamStep<Flux<OpenAiStreamCompletionResponse>> {

    @Override
    public StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> apply(
            Flux<OpenAiStreamCompletionResponse> upstream,
            StreamAgentContext<?> context
    ) {
        Objects.requireNonNull(upstream, "upstream must not be null");
        Objects.requireNonNull(context, "context must not be null");

        StreamRoundState roundState = context.currentRound();

        OpenAiStreamRoundAccumulator accumulator =
                OpenAiStreamRoundSupport.getOrCreateAccumulator(context);

        AtomicBoolean finishSignalSeen = new AtomicBoolean(false);

        Flux<OpenAiStreamCompletionResponse> analyzedStream = upstream
                .doOnNext(chunk -> {
                    if (roundState.isRoundComplete()
                            || chunk == null
                            || chunk.getChoices() == null
                            || chunk.getChoices().isEmpty()) {
                        return;
                    }

                    boolean finished = chunk.getChoices()
                            .stream()
                            .anyMatch(choice ->
                                    choice != null
                                            && choice.getFinishReason() != null
                            );

                    if (!finished) {
                        return;
                    }

                    finishSignalSeen.set(true);

                    OpenAiStreamCompletionResponse finalResponse =
                            accumulator.tryToFinalResponse();

                    if (finalResponse == null) {
                        context.setTerminationReason(
                                AgentTerminationReason.MODEL_ERROR
                        );
                        roundState.setRoundComplete(true);
                        return;
                    }

                    roundState.setFinalResponse(finalResponse);
                    roundState.setRoundComplete(true);
                });

        Flux<OpenAiStreamCompletionResponse> streamWithCompletionSignal =
                analyzedStream.concatWith(
                        Flux.defer(() -> {
                            /*
                             * 标准供应商已经通过 finishReason 触发过 expand，
                             * 不需要再次追加哨兵。
                             */
                            if (finishSignalSeen.get()
                                    || roundState.isRoundComplete()) {
                                return Flux.empty();
                            }

                            OpenAiStreamCompletionResponse finalResponse =
                                    accumulator.tryToFinalResponse();

                            /*
                             * 完全没有有效响应时不能构造哨兵，
                             * 直接让订阅失败，避免空流被误判为正常完成。
                             */
                            if (finalResponse == null) {
                                context.setTerminationReason(
                                        AgentTerminationReason.MODEL_ERROR
                                );
                                return Flux.error(
                                        new IllegalStateException(
                                                "OpenAI stream completed without a valid response"
                                        )
                                );
                            }

                            roundState.setFinalResponse(finalResponse);
                            roundState.setRoundComplete(true);

                            /*
                             * 哨兵必须是独立对象，不能直接复用 finalResponse，
                             * 否则它可能被误认为真实的对外响应 chunk。
                             */
                            OpenAiStreamCompletionResponse sentinel =
                                    new OpenAiStreamCompletionResponse(
                                            finalResponse.getBaseResponse(),
                                            List.of(),
                                            finalResponse.getUsage()
                                    );

                            context.setControlSignal(sentinel);

                            return Flux.just(sentinel);
                        })
                );

        return new StreamApplyResult<>(
                streamWithCompletionSignal,
                StreamStepKey.STREAM_END
        );
    }
}