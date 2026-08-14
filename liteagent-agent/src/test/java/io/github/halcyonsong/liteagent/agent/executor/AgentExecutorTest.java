package io.github.halcyonsong.liteagent.agent.executor;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentExecutorTest {

    @Test
    void execute_should_run_registered_steps_in_order() {
        List<AgentStepKey> executionOrder = new ArrayList<>();
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> {
            executionOrder.add(AgentStepKey.BEGIN);
            return AgentStepKey.MAP_REQUEST;
        });
        steps.put(AgentStepKey.MAP_REQUEST, context -> {
            executionOrder.add(AgentStepKey.MAP_REQUEST);
            context.setAttribute("mapped", true);
            return AgentStepKey.BUILD_RESULT;
        });
        steps.put(AgentStepKey.BUILD_RESULT, context -> {
            executionOrder.add(AgentStepKey.BUILD_RESULT);
            return AgentStepKey.END;
        });
        steps.put(AgentStepKey.END, context -> AgentStepKey.END);

        AgentExecutor executor = new AgentExecutor(steps);
        AgentContext context = AgentContext.create(new TestInvocation());

        AgentContext result = executor.execute(context);

        assertSame(context, result);
        assertEquals(List.of(AgentStepKey.BEGIN, AgentStepKey.MAP_REQUEST, AgentStepKey.BUILD_RESULT), executionOrder);
        assertEquals(Boolean.TRUE, context.getAttribute("mapped"));
    }

    @Test
    void execute_should_trigger_hooks() {
        List<String> events = new ArrayList<>();
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> AgentStepKey.END);
        steps.put(AgentStepKey.END, context -> AgentStepKey.END);

        StepHook hook = new StepHook() {
            @Override
            public void beforeStep(AgentStepKey key, AgentContext context) {
                events.add("before:" + key);
            }

            @Override
            public void afterStep(AgentStepKey key, AgentContext context, AgentStepKey nextKey) {
                events.add("after:" + key + "->" + nextKey);
            }
        };

        AgentExecutor executor = new AgentExecutor(steps, List.of(hook), 10);
        executor.execute(AgentContext.create(new TestInvocation()));

        assertEquals(List.of("before:BEGIN", "after:BEGIN->END"), events);
    }

    @Test
    void execute_should_trigger_error_hook_and_rethrow() {
        List<String> events = new ArrayList<>();
        RuntimeException failure = new RuntimeException("boom");
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> {
            throw failure;
        });

        StepHook hook = new StepHook() {
            @Override
            public void onStepError(AgentStepKey key, AgentContext context, Throwable error) {
                events.add(key + ":" + error.getMessage());
            }
        };

        AgentExecutor executor = new AgentExecutor(steps, List.of(hook), 10);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> executor.execute(AgentContext.create(new TestInvocation())));

        assertSame(failure, thrown);
        assertEquals(List.of("BEGIN:boom"), events);
    }

    @Test
    void execute_should_fail_when_step_missing() {
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        AgentExecutor executor = new AgentExecutor(steps, List.of(), 10);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> executor.execute(AgentContext.create(new TestInvocation())));

        assertTrue(exception.getMessage().contains("No agent step registered for key: BEGIN"));
    }

    @Test
    void execute_should_fail_when_step_limit_exceeded() {
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> AgentStepKey.BEGIN);

        AgentExecutor executor = new AgentExecutor(steps, List.of(), 2);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> executor.execute(AgentContext.create(new TestInvocation())));

        assertTrue(exception.getMessage().contains("Agent step limit exceeded: 2"));
    }

    @Test
    void constructor_should_validate_arguments() {
        assertThrows(NullPointerException.class, () -> new AgentExecutor(null, List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new AgentExecutor(Map.of(), List.of(), 0));
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
