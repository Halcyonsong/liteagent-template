package io.github.halcyonsong.liteagent.agent;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.executor.AgentExecutor;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.BaseRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.Result;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    void execute_should_return_final_result() {
        TestResult expected = new TestResult();
        Agent agent = new Agent(new AgentExecutor(createSteps(expected)));

        Result result = agent.execute(new TestInvocation());

        assertSame(expected, result);
    }

    @Test
    void execute_context_should_return_context_with_result() {
        TestResult expected = new TestResult();
        Agent agent = new Agent(new AgentExecutor(createSteps(expected)));

        AgentContext context = agent.executeContext(new TestInvocation());

        assertSame(expected, context.getResult());
    }

    @Test
    void constructor_should_reject_null_executor() {
        assertThrows(NullPointerException.class, () -> new Agent(null));
    }

    private static Map<AgentStepKey, AgentStep> createSteps(TestResult result) {
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> {
            context.setResult(result);
            return AgentStepKey.END;
        });
        steps.put(AgentStepKey.END, context -> AgentStepKey.END);
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
