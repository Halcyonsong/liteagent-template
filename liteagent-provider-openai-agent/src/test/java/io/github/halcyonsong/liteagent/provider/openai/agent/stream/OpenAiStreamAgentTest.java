package io.github.halcyonsong.liteagent.provider.openai.agent.stream;

import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamAgentTest {

    @Test
    void execute_should_return_stream_of_chunks() {
        OpenAiStreamCompletionResponse expectedChunk = createChunk("hello", FinishReason.STOP);
        OpenAiStreamAgent agent = new OpenAiStreamAgent(new StreamAgent<>(createExecutor(expectedChunk)));

        List<OpenAiStreamCompletionResponse> results = agent.execute(createRequest()).collectList().block();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(expectedChunk, results.get(0));
    }

    @Test
    void execute_context_should_return_context_with_output() {
        OpenAiStreamCompletionResponse expectedChunk = createChunk("hello", FinishReason.STOP);
        OpenAiStreamAgent agent = new OpenAiStreamAgent(new StreamAgent<>(createExecutor(expectedChunk)));

        StreamAgentContext<OpenAiStreamCompletionResponse> context = agent.executeContext(createRequest());

        assertNotNull(context.getOutput());
        assertFalse(context.getRounds().isEmpty());
    }

    @Test
    void constructor_should_reject_null_agent() {
        assertThrows(NullPointerException.class, () -> new OpenAiStreamAgent(null));
    }

    private static StreamAgentExecutor<OpenAiStreamCompletionResponse> createExecutor(
            OpenAiStreamCompletionResponse chunk) {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new EnumMap<>(StreamStepKey.class);
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.INIT_WORKING_MESSAGES);
        syncSteps.put(StreamStepKey.INIT_WORKING_MESSAGES, ctx -> StreamStepKey.MAP_REQUEST);
        syncSteps.put(StreamStepKey.MAP_REQUEST, ctx -> StreamStepKey.ENHANCE_REQUEST);
        syncSteps.put(StreamStepKey.ENHANCE_REQUEST, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> StreamStepKey.BUILD_RESULT);
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> StreamStepKey.END);
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<OpenAiStreamCompletionResponse>>> streamSteps = new EnumMap<>(StreamStepKey.class);
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) ->
                new StreamApplyResult<>(Flux.just(chunk), StreamStepKey.ENHANCE_CHUNK));
        streamSteps.put(StreamStepKey.ENHANCE_CHUNK, (upstream, ctx) ->
                new StreamApplyResult<>(upstream, StreamStepKey.ACCUMULATE_CHUNK));
        streamSteps.put(StreamStepKey.ACCUMULATE_CHUNK, (upstream, ctx) ->
                new StreamApplyResult<>(upstream, StreamStepKey.ANALYZE_CHUNK));
        streamSteps.put(StreamStepKey.ANALYZE_CHUNK, (upstream, ctx) -> {
            ctx.currentRound().setRoundComplete(true);
            ctx.currentRound().setFinalResponse(chunk);
            return new StreamApplyResult<>(upstream, StreamStepKey.STREAM_END);
        });

        return new StreamAgentExecutor<>(syncSteps, streamSteps);
    }

    private static OpenAiChatCompletionRequest createRequest() {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .build();
    }

    private static OpenAiStreamCompletionResponse createChunk(String content, FinishReason finishReason) {
        return new OpenAiStreamCompletionResponse(
                new OpenAiBaseResponse("resp-1", "chat.completion.chunk", 123L, "test-model"),
                List.of(new StreamChoice(0, new StreamDelta("assistant", content, null), finishReason)),
                null
        );
    }
}
