package io.github.halcyonsong.liteagent.provider.openai.agent;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
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

class OpenAiChatAgentTest {

    @Test
    void execute_should_return_provider_response() {
        OpenAiChatCompletionResponse expected = createResponse();
        OpenAiChatAgent agent = new OpenAiChatAgent(new ChatAgent(new ChatAgentExecutor(createSteps(expected))));
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

    private static Map<ChatStepKey, ChatStep> createSteps(OpenAiChatCompletionResponse response) {
        Map<ChatStepKey, ChatStep> steps = new EnumMap<>(ChatStepKey.class);
        steps.put(ChatStepKey.BEGIN, context -> {
            context.setResult(response);
            return ChatStepKey.END;
        });
        steps.put(ChatStepKey.END, context -> ChatStepKey.END);
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
