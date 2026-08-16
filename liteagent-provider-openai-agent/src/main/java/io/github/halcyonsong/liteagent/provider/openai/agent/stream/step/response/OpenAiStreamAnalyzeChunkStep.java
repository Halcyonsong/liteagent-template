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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 分析当前轮流式 chunk，并在收到结束信号时标记 round 完成。
 * <p>
 * 标准结束依赖 finishReason；
 * 对未返回 finishReason 的流，追加内部哨兵触发一次收尾判断。
 */
@Slf4j
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
                        log.warn(
                                "Detected stream finishReason but aggregated final response is missing. " +
                                        "executionId={}, " +
                                        "roundIndex={}",
                                context.getExecutionId(),
                                roundState.getRoundIndex()
                        );

                        context.setTerminationReason(
                                AgentTerminationReason.MODEL_ERROR
                        );
                        roundState.setRoundComplete(true);
                        return;
                    }

                    log.debug(
                            "Detected stream round completion from finishReason. " +
                                    "executionId={}, " +
                                    "roundIndex={}, " +
                                    "responseId={}, " +
                                    "hasFinalResponse={}",
                            context.getExecutionId(),
                            roundState.getRoundIndex(),
                            finalResponse.getBaseResponse() == null ? null : finalResponse.getBaseResponse().getId(),
                            true
                    );

                    roundState.setFinalResponse(finalResponse);
                    roundState.setRoundComplete(true);
                });

        Flux<OpenAiStreamCompletionResponse> streamWithCompletionSignal =
                analyzedStream.concatWith(
                        Flux.defer(() -> {
                            /*
                             * 标准供应商已经通过任何 emitted chunk 触发过 expand，
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
                                log.warn(
                                        "Stream completed without a valid aggregated response. " +
                                                "executionId={}, " +
                                                "roundIndex={}",
                                        context.getExecutionId(),
                                        roundState.getRoundIndex()
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

                            log.debug(
                                    "Injected stream completion sentinel. " +
                                            "executionId={}, " +
                                            "roundIndex={}, " +
                                            "responseId={}",
                                    context.getExecutionId(),
                                    roundState.getRoundIndex(),
                                    finalResponse.getBaseResponse() == null ? null : finalResponse.getBaseResponse().getId()
                            );

                            return Flux.just(sentinel);
                        })
                );

        return new StreamApplyResult<>(
                streamWithCompletionSignal,
                StreamStepKey.STREAM_END
        );
    }
}