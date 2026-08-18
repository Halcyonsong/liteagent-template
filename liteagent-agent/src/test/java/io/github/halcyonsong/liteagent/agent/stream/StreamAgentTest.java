package io.github.halcyonsong.liteagent.agent.stream;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamApplyResult;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StreamAgentTest {

    @Test
    void execute_should_return_flux() {
        StreamAgent<String> agent = new StreamAgent<>(createExecutor());

        List<String> results = agent.execute(new TestInvocation()).collectList().block();

        assertNotNull(results);
        assertEquals(List.of("hello"), results);
    }

    @Test
    void execute_context_should_return_context_with_output() {
        StreamAgent<String> agent = new StreamAgent<>(createExecutor());

        StreamAgentContext<String> context = agent.executeContext(new TestInvocation());

        assertNotNull(context.getOutput());
        context.getOutput().blockLast();
        assertNotNull(context.getRounds());
        assertFalse(context.getRounds().isEmpty());
    }

    @Test
    void constructor_should_reject_null_executor() {
        assertThrows(NullPointerException.class, () -> new StreamAgent<String>(null));
    }

    private static StreamAgentExecutor<String> createExecutor() {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, ctx -> StreamStepKey.SEND_REQUEST);
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, ctx -> StreamStepKey.BUILD_RESULT);
        syncSteps.put(StreamStepKey.BUILD_RESULT, ctx -> StreamStepKey.END);
        syncSteps.put(StreamStepKey.END, ctx -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<String>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, (upstream, ctx) ->
                new StreamApplyResult<>(Flux.just("hello"), StreamStepKey.STREAM_END));

        return new StreamAgentExecutor<>(syncSteps, streamSteps);
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
