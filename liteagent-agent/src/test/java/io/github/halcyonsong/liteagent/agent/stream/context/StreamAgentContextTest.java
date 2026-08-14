package io.github.halcyonsong.liteagent.agent.stream.context;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreamAgentContextTest {

    @Test
    void create_should_initialize_fields() {
        Invocation invocation = new TestInvocation();

        StreamAgentContext<String> context = StreamAgentContext.create("exec-1", invocation);

        assertEquals("exec-1", context.getExecutionId());
        assertSame(invocation, context.getInvocation());
        assertNotNull(context.getAttributes());
        assertTrue(context.getAttributes().isEmpty());
        assertTrue(context.getWorkingMessages().isEmpty());
        assertTrue(context.getRounds().isEmpty());
        assertEquals(0, context.getIteration());
        assertEquals(10, context.getMaxIterations());
        assertNull(context.getTerminationReason());
        assertNull(context.getOutput());
    }

    @Test
    void create_should_reject_invalid_arguments() {
        Invocation invocation = new TestInvocation();

        assertThrows(IllegalArgumentException.class, () -> StreamAgentContext.create("", invocation));
        assertThrows(IllegalArgumentException.class, () -> StreamAgentContext.create("  ", invocation));
        assertThrows(IllegalArgumentException.class, () -> StreamAgentContext.create("exec", null));
    }

    @Test
    void working_messages_should_support_clear_and_append() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        context.appendWorkingMessage(Messages.user("hello"));
        context.appendWorkingMessage(Messages.user("world"));
        assertEquals(2, context.getWorkingMessages().size());

        context.clearWorkingMessages();
        assertTrue(context.getWorkingMessages().isEmpty());

        context.appendWorkingMessages(java.util.List.of(Messages.user("a"), Messages.user("b")));
        assertEquals(2, context.getWorkingMessages().size());
    }

    @Test
    void rounds_should_support_add_current_and_get_by_index() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        StreamRoundState round0 = new StreamRoundState(0);
        context.addRound(round0);
        assertSame(round0, context.currentRound());
        assertSame(round0, context.getRound(0));

        StreamRoundState round1 = new StreamRoundState(1);
        context.addRound(round1);
        assertSame(round1, context.currentRound());
        assertSame(round1, context.getRound(1));
    }

    @Test
    void current_round_should_throw_when_no_rounds() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        assertThrows(IllegalStateException.class, context::currentRound);
    }

    @Test
    void attributes_should_put_replace_and_remove() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        context.setAttribute("k1", "v1");
        assertEquals("v1", context.getAttribute("k1"));
        assertEquals("v1", context.getAttribute("k1", String.class));

        context.setAttribute("k1", "v2");
        assertEquals("v2", context.getAttribute("k1"));

        context.setAttribute("k1", null);
        assertNull(context.getAttribute("k1"));
    }

    @Test
    void iteration_and_max_iterations_should_work() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        context.incrementIteration();
        context.incrementIteration();
        assertEquals(2, context.getIteration());

        context.setMaxIterations(5);
        assertEquals(5, context.getMaxIterations());

        assertThrows(IllegalArgumentException.class, () -> context.setMaxIterations(0));
    }

    @Test
    void termination_reason_should_be_settable() {
        StreamAgentContext<String> context = StreamAgentContext.create(new TestInvocation());

        context.setTerminationReason(AgentTerminationReason.COMPLETED);
        assertEquals(AgentTerminationReason.COMPLETED, context.getTerminationReason());
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
