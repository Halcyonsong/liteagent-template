package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.state.OpenAiStreamRoundAccumulator;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamRoundSupport;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamInitWorkingMessagesStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamAccumulateChunkStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamAnalyzeChunkStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamBuildResultStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamDecideNextActionStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamExecuteToolStep;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamStepsTest {

    @Test
    void begin_step_should_route_to_init_on_first_round() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamBeginStep step = new OpenAiStreamBeginStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.INIT_WORKING_MESSAGES, next);
    }

    @Test
    void begin_step_should_route_to_map_request_when_messages_exist() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        ctx.appendWorkingMessage(Messages.user("existing"));
        OpenAiStreamBeginStep step = new OpenAiStreamBeginStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.MAP_REQUEST, next);
    }

    @Test
    void init_working_messages_should_copy_invocation_messages() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamInitWorkingMessagesStep step = new OpenAiStreamInitWorkingMessagesStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.MAP_REQUEST, next);
        assertEquals(1, ctx.getWorkingMessages().size());
    }

    @Test
    void map_request_should_build_provider_and_raw_request() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        ctx.appendWorkingMessage(Messages.user("hello"));
        OpenAiStreamMapRequestStep step = new OpenAiStreamMapRequestStep(new OpenAiChatRequestMapper());

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.ENHANCE_REQUEST, next);
        assertNotNull(ctx.getAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST, OpenAiChatCompletionRequest.class));
        assertNotNull(ctx.getAttribute(OpenAiAgentAttributes.RAW_REQUEST, OpenAiChatCompletionRawRequest.class));
    }

    @Test
    void map_request_should_fail_when_working_messages_empty() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamMapRequestStep step = new OpenAiStreamMapRequestStep(new OpenAiChatRequestMapper());

        assertThrows(IllegalStateException.class, () -> step.invoke(ctx));
    }

    @Test
    void enhance_request_should_apply_advisors_and_set_stream_true() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiChatCompletionRequest providerRequest = createProviderRequest();
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();
        ctx.setAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST, providerRequest);
        ctx.setAttribute(OpenAiAgentAttributes.RAW_REQUEST, rawRequest);

        OpenAiStreamEnhanceRequestStep step = new OpenAiStreamEnhanceRequestStep(new OpenAiAdvisorsSupport());
        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.SEND_REQUEST, next);
        assertEquals(Boolean.TRUE, rawRequest.getStream());
    }

    @Test
    void enhance_request_should_fail_when_attributes_missing() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamEnhanceRequestStep step = new OpenAiStreamEnhanceRequestStep(new OpenAiAdvisorsSupport());

        assertThrows(IllegalStateException.class, () -> step.invoke(ctx));
    }

    @Test
    void accumulate_chunk_should_accumulate_via_doOnNext() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        ctx.addRound(new StreamRoundState(0));

        OpenAiStreamAccumulateChunkStep step = new OpenAiStreamAccumulateChunkStep();
        OpenAiStreamCompletionResponse chunk = createChunk("hello", null);

        StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> result =
                step.apply(Flux.just(chunk), ctx);

        result.getOutput().blockLast();

        StreamRoundState roundState = ctx.currentRound();
        OpenAiStreamRoundAccumulator acc = (OpenAiStreamRoundAccumulator) roundState.getAccumulator();
        assertNotNull(acc);
        assertTrue(acc.hasChoices());
    }

    @Test
    void analyze_chunk_should_set_round_complete_on_stream_end() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        StreamRoundState roundState = new StreamRoundState(0);
        ctx.addRound(roundState);

        OpenAiStreamAnalyzeChunkStep step = new OpenAiStreamAnalyzeChunkStep();
        OpenAiStreamCompletionResponse chunk = createChunk("hello", FinishReason.STOP);
        OpenAiStreamRoundSupport.getOrCreateAccumulator(ctx).accumulate(chunk);

        StreamApplyResult<Flux<OpenAiStreamCompletionResponse>> result =
                step.apply(Flux.just(chunk), ctx);

        assertEquals(StreamStepKey.STREAM_END, result.getNextKey());

        result.getOutput().blockLast();

        assertTrue(roundState.isRoundComplete());
        assertNotNull(roundState.getFinalResponse());
    }

    @Test
    void decide_next_action_should_default_to_build_result() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamDecideNextActionStep step = new OpenAiStreamDecideNextActionStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.BUILD_RESULT, next);
    }

    @Test
    void execute_tool_should_return_end() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamExecuteToolStep step = new OpenAiStreamExecuteToolStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.END, next);
    }

    @Test
    void build_result_should_set_termination_reason() {
        StreamAgentContext<OpenAiStreamCompletionResponse> ctx = createContext();
        OpenAiStreamBuildResultStep step = new OpenAiStreamBuildResultStep();

        StreamStepKey next = step.invoke(ctx);

        assertEquals(StreamStepKey.END, next);
        assertNotNull(ctx.getTerminationReason());
    }

    private static StreamAgentContext<OpenAiStreamCompletionResponse> createContext() {
        return StreamAgentContext.create(createProviderRequest());
    }

    private static OpenAiChatCompletionRequest createProviderRequest() {
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
                List.of(new StreamChoice(0, new StreamDelta("assistant", content, null, List.of()), finishReason)),
                null
        );
    }
}
