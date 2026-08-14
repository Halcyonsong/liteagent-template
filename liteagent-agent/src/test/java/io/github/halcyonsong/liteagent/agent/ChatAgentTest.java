package io.github.halcyonsong.liteagent.agent;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChatAgentTest {

    @Test
    void execute_should_return_final_result() {
        TestResult expected = new TestResult();
        ChatAgent chatAgent = new ChatAgent(new ChatAgentExecutor(createSteps(expected)));

        Result result = chatAgent.execute(new TestInvocation());

        assertSame(expected, result);
    }

    @Test
    void execute_context_should_return_context_with_result() {
        TestResult expected = new TestResult();
        ChatAgent chatAgent = new ChatAgent(new ChatAgentExecutor(createSteps(expected)));

        ChatAgentContext context = chatAgent.executeContext(new TestInvocation());

        assertSame(expected, context.getResult());
    }

    @Test
    void constructor_should_reject_null_executor() {
        assertThrows(NullPointerException.class, () -> new ChatAgent(null));
    }

    private static Map<ChatStepKey, ChatStep> createSteps(TestResult result) {
        Map<ChatStepKey, ChatStep> steps = new EnumMap<>(ChatStepKey.class);
        steps.put(ChatStepKey.BEGIN, context -> {
            context.setResult(result);
            return ChatStepKey.END;
        });
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);
        return steps;
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
