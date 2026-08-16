package io.github.halcyonsong.liteagent.agent.chat.executor;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChatChatAgentExecutorTest {

    @Test
    void execute_should_run_registered_steps_in_order() {
        List<ChatStepKey> executionOrder = new ArrayList<>();
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, context -> {
            executionOrder.add(ChatStepKey.BEGIN);
            return ChatStepKey.MAP_REQUEST;
        });
        steps.put(ChatStepKey.MAP_REQUEST, context -> {
            executionOrder.add(ChatStepKey.MAP_REQUEST);
            context.setAttribute("mapped", true);
            return ChatStepKey.BUILD_RESULT;
        });
        steps.put(ChatStepKey.BUILD_RESULT, context -> {
            executionOrder.add(ChatStepKey.BUILD_RESULT);
            return ChatStepKey.END;
        });
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);

        ChatAgentExecutor executor = new ChatAgentExecutor(steps);
        ChatAgentContext context = ChatAgentContext.create(new TestInvocation());

        ChatAgentContext result = executor.execute(context);

        assertSame(context, result);
        assertEquals(List.of(ChatStepKey.BEGIN, ChatStepKey.MAP_REQUEST, ChatStepKey.BUILD_RESULT), executionOrder);
        assertEquals(Boolean.TRUE, context.getAttribute("mapped"));
    }

    @Test
    void execute_should_trigger_hooks() {
        List<String> events = new ArrayList<>();
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, context -> ChatStepKey.END);
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);

        StepHook hook = new StepHook() {
            @Override
            public void beforeStep(ChatStepKey key, ChatAgentContext context) {
                events.add("before:" + key.name());
            }

            @Override
            public void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
                events.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        ChatAgentExecutor executor = new ChatAgentExecutor(steps, List.of(hook), 10);
        executor.execute(ChatAgentContext.create(new TestInvocation()));

        assertEquals(List.of("before:BEGIN", "after:BEGIN->END", "before:END", "after:END->END"), events);
    }

    @Test
    void execute_should_trigger_error_hook_and_rethrow() {
        List<String> events = new ArrayList<>();
        RuntimeException failure = new RuntimeException("boom");
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, context -> {
            throw failure;
        });

        StepHook hook = new StepHook() {
            @Override
            public void onStepError(ChatStepKey key, ChatAgentContext context, Throwable error) {
                events.add(key.name() + ":" + error.getMessage());
            }
        };

        ChatAgentExecutor executor = new ChatAgentExecutor(steps, List.of(hook), 10);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> executor.execute(ChatAgentContext.create(new TestInvocation())));

        assertSame(failure, thrown);
        assertEquals(List.of("BEGIN:boom"), events);
    }

    @Test
    void execute_should_fail_when_step_missing() {
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        ChatAgentExecutor executor = new ChatAgentExecutor(steps, List.of(), 10);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> executor.execute(ChatAgentContext.create(new TestInvocation())));

        assertTrue(exception.getMessage().contains("No agent step registered for key"));
        assertTrue(exception.getMessage().contains("BEGIN"));
    }

    @Test
    void execute_should_fail_when_step_limit_exceeded() {
        Map<ChatStepKey, ChatStep> steps = new HashMap<>();
        steps.put(ChatStepKey.BEGIN, context -> ChatStepKey.BEGIN);

        ChatAgentExecutor executor = new ChatAgentExecutor(steps, List.of(), 2);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> executor.execute(ChatAgentContext.create(new TestInvocation())));

        assertTrue(exception.getMessage().contains("ChatAgent step limit exceeded: 2"));
    }

    @Test
    void constructor_should_validate_arguments() {
        assertThrows(NullPointerException.class, () -> new ChatAgentExecutor(null, List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new ChatAgentExecutor(Map.of(), List.of(), 0));
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
