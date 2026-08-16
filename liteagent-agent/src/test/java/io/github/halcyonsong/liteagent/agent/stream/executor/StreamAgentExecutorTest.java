package io.github.halcyonsong.liteagent.agent.stream.executor;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StreamAgentExecutorTest {

    @Test
    void execute_should_build_single_round_stream() {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> StreamStepKey.BUILD_RESULT);
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> {
            ctx.setTerminationReason(AgentTerminationReason.COMPLETED);
            return StreamStepKey.END;
        });
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) -> {
            assertNotNull(ctx.currentRound());
            return new StreamApplyResult<>(Flux.just("chunk1", "chunk2"), StreamStepKey.ANALYZE_CHUNK);
        });
        streamSteps.put(StreamStepKey.ANALYZE_CHUNK, (upstream, ctx) -> {
            ctx.currentRound().setRoundComplete(true);
            return new StreamApplyResult<>(upstream, StreamStepKey.STREAM_END);
        });

        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(syncSteps, streamSteps);
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        List<String> results = executor.execute(context).collectList().block();

        assertNotNull(results);
        assertEquals(List.of("chunk1", "chunk2"), results);
        assertEquals(1, context.getRounds().size());
        assertNotNull(context.getTerminationReason());
    }

    @Test
    void execute_should_run_sync_steps_in_order() {
        List<StreamStepKey> executionOrder = new ArrayList<>();

        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> {
            executionOrder.add(StreamStepKey.BEGIN);
            return StreamStepKey.MAP_REQUEST;
        });
        syncSteps.put(StreamStepKey.MAP_REQUEST, ctx -> {
            executionOrder.add(StreamStepKey.MAP_REQUEST);
            return StreamStepKey.SEND_REQUEST;
        });
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> {
            executionOrder.add(StreamStepKey.DECIDE_NEXT_ACTION);
            return StreamStepKey.BUILD_RESULT;
        });
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> {
            executionOrder.add(StreamStepKey.BUILD_RESULT);
            return StreamStepKey.END;
        });
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) ->
                new StreamApplyResult<>(Flux.just("data"), StreamStepKey.ANALYZE_CHUNK));
        streamSteps.put(StreamStepKey.ANALYZE_CHUNK, (upstream, ctx) -> {
            ctx.currentRound().setRoundComplete(true);
            return new StreamApplyResult<>(upstream, StreamStepKey.STREAM_END);
        });

        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(syncSteps, streamSteps);
        executor.execute(StreamAgentContext.create(new TestInvocation())).blockLast();

        assertEquals(List.of(
                StreamStepKey.BEGIN,
                StreamStepKey.MAP_REQUEST,
                StreamStepKey.DECIDE_NEXT_ACTION,
                StreamStepKey.BUILD_RESULT
        ), executionOrder);
    }

    @Test
    void execute_should_trigger_hooks() {
        List<String> events = new ArrayList<>();

        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> StreamStepKey.BUILD_RESULT);
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> {
            ctx.setTerminationReason(AgentTerminationReason.COMPLETED);
            return StreamStepKey.END;
        });
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) ->
                new StreamApplyResult<>(Flux.just("x"), StreamStepKey.ANALYZE_CHUNK));
        streamSteps.put(StreamStepKey.ANALYZE_CHUNK, (upstream, ctx) -> {
            ctx.currentRound().setRoundComplete(true);
            return new StreamApplyResult<>(upstream, StreamStepKey.STREAM_END);
        });

        StreamStepHook hook = new StreamStepHook() {
            @Override
            public void beforeStep(StreamStepKey key, StreamAgentContext<?> context) {
                events.add("before:" + key.name());
            }

            @Override
            public void afterStep(StreamStepKey key, StreamAgentContext<?> context, StreamStepKey nextKey) {
                events.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(hook), 100);
        executor.execute(StreamAgentContext.create(new TestInvocation())).blockLast();

        assertTrue(events.contains("before:BEGIN"));
        assertTrue(events.contains("after:BEGIN->SEND_REQUEST"));
        assertTrue(events.contains("before:SEND_REQUEST"));
        assertTrue(events.contains("after:SEND_REQUEST->ANALYZE_CHUNK"));
        assertTrue(events.contains("before:ANALYZE_CHUNK"));
        assertTrue(events.contains("after:ANALYZE_CHUNK->STREAM_END"));
        assertTrue(events.contains("before:DECIDE_NEXT_ACTION"));
        assertTrue(events.contains("before:BUILD_RESULT"));
    }

    @Test
    void execute_should_fail_when_sync_step_missing() {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();

        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(), 10);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> executor.execute(StreamAgentContext.create(new TestInvocation())));
        assertTrue(ex.getMessage().contains("No stream sync step registered for key"));
        assertTrue(ex.getMessage().contains("BEGIN"));
    }

    @Test
    void execute_should_fail_when_stream_step_missing() {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();

        StreamAgentExecutor<String> executor = new StreamAgentExecutor<>(syncSteps, streamSteps, List.of(), 10);

        assertThrows(IllegalStateException.class,
                () -> executor.execute(StreamAgentContext.create(new TestInvocation())));
    }

    @Test
    void constructor_should_validate_arguments() {
        assertThrows(NullPointerException.class,
                () -> new StreamAgentExecutor<String>(null, Map.of(), List.of(), 1));
        assertThrows(NullPointerException.class,
                () -> new StreamAgentExecutor<String>(Map.of(), null, List.of(), 1));
        assertThrows(IllegalArgumentException.class,
                () -> new StreamAgentExecutor<String>(Map.of(), Map.of(), List.of(), 0));
    }

    private static class TestInvocation implements Invocation {
        private final BaseRequest baseRequest = new TestBaseRequest();
        private final ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("hello"))
                .build();

        @Override
        public BaseRequest getBaseRequest() {
            return baseRequest;
        }

        @Override
        public ChatRequest getChatRequest() {
            return chatRequest;
        }
    }

    private static class TestBaseRequest implements BaseRequest {
        @Override
        public String getBaseUrl() {
            return "https://example.com";
        }

        @Override
        public String getApiKey() {
            return "test-key";
        }

        @Override
        public String getModel() {
            return "test-model";
        }
    }
}
