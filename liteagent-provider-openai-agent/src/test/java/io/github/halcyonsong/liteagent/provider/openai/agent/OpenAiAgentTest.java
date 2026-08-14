package io.github.halcyonsong.liteagent.provider.openai.agent;

import io.github.halcyonsong.liteagent.agent.Agent;
import io.github.halcyonsong.liteagent.agent.executor.AgentExecutor;
import io.github.halcyonsong.liteagent.agent.step.AgentStep;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiAgentTest {

    @Test
    void execute_should_return_provider_response() {
        OpenAiChatCompletionResponse expected = createResponse();
        OpenAiAgent agent = new OpenAiAgent(new Agent(new AgentExecutor(createSteps(expected))));
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .build();

        OpenAiChatCompletionResponse response = agent.execute(request);

        assertSame(expected, response);
    }

    private static Map<AgentStepKey, AgentStep> createSteps(OpenAiChatCompletionResponse response) {
        Map<AgentStepKey, AgentStep> steps = new EnumMap<>(AgentStepKey.class);
        steps.put(AgentStepKey.BEGIN, context -> {
            context.setResult(response);
            return AgentStepKey.END;
        });
        steps.put(AgentStepKey.END, context -> AgentStepKey.END);
        return steps;
    }

    private static OpenAiChatCompletionResponse createResponse() {
        return new OpenAiChatCompletionResponse(
                new OpenAiBaseResponse("resp-1", "chat.completion", 123L, "test-model"),
                List.of(new ChatChoice(
                        0,
                        new ChatResponse(List.of(new AssistantMessage("hello"))),
                        FinishReason.STOP
                )),
                new OpenAiUsage(1, 1, 2, null, null, null, null)
        );
    }
}
