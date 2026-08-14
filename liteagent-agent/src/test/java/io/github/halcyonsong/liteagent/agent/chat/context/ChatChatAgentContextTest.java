package io.github.halcyonsong.liteagent.agent.chat.context;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatChatAgentContextTest {

    @Test
    void create_should_initialize_context_fields() {
        Invocation invocation = new TestInvocation();

        ChatAgentContext context = ChatAgentContext.create("exec-1", invocation);

        assertEquals("exec-1", context.getExecutionId());
        assertSame(invocation, context.getInvocation());
        assertNotNull(context.getAttributes());
        assertTrue(context.getAttributes().isEmpty());
        assertEquals(0, context.getIteration());
        assertEquals(10, context.getMaxIterations());
        assertNull(context.getResult());
        assertNull(context.getTerminationReason());
    }

    @Test
    void set_attribute_should_put_replace_and_remove_value() {
        ChatAgentContext context = ChatAgentContext.create(new TestInvocation());

        context.setAttribute("k1", "v1");
        assertEquals("v1", context.getAttribute("k1"));
        assertEquals("v1", context.getAttribute("k1", String.class));

        context.setAttribute("k1", "v2");
        assertEquals("v2", context.getAttribute("k1"));

        context.setAttribute("k1", null);
        assertNull(context.getAttribute("k1"));
    }

    @Test
    void increment_iteration_and_set_max_iterations_should_work() {
        ChatAgentContext context = ChatAgentContext.create(new TestInvocation());

        context.incrementIteration();
        context.incrementIteration();
        context.setMaxIterations(5);

        assertEquals(2, context.getIteration());
        assertEquals(5, context.getMaxIterations());
    }

    @Test
    void create_should_reject_invalid_arguments() {
        Invocation invocation = new TestInvocation();

        assertThrows(IllegalArgumentException.class, () -> ChatAgentContext.create("", invocation));
        assertThrows(IllegalArgumentException.class, () -> ChatAgentContext.create("  ", invocation));
        assertThrows(IllegalArgumentException.class, () -> ChatAgentContext.create("exec", null));
    }

    @Test
    void set_attribute_and_set_max_iterations_should_validate_arguments() {
        ChatAgentContext context = ChatAgentContext.create(new TestInvocation());

        assertThrows(IllegalArgumentException.class, () -> context.setAttribute(null, "v"));
        assertThrows(IllegalArgumentException.class, () -> context.setAttribute(" ", "v"));
        assertThrows(IllegalArgumentException.class, () -> context.setMaxIterations(0));
    }

    @Test
    void result_setter_should_store_result() {
        ChatAgentContext context = ChatAgentContext.create(new TestInvocation());
        Result result = new TestResult();

        context.setResult(result);

        assertSame(result, context.getResult());
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

    private static class TestResult implements Result {
    }
}
