package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiEnhanceRequestStepTest {

    @Test
    void invoke_should_apply_advisors_and_set_stream_false() {
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .requestAdvisor((providerRequest, rawRequest) -> rawRequest.setTemperature(0.5))
                .build();

        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();
        AgentContext context = AgentContext.create(request);
        context.setAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST, request);
        context.setAttribute(OpenAiAgentAttributes.RAW_REQUEST, rawRequest);

        OpenAiEnhanceRequestStep step = new OpenAiEnhanceRequestStep(new OpenAiClientSupport());

        AgentStepKey next = step.invoke(context);

        assertEquals(AgentStepKey.SEND_CHAT_REQUEST, next);
        assertEquals(0.5, rawRequest.getTemperature());
        assertEquals(Boolean.FALSE, rawRequest.getStream());
    }

    @Test
    void invoke_should_fail_when_context_missing_required_attributes() {
        OpenAiEnhanceRequestStep step = new OpenAiEnhanceRequestStep(new OpenAiClientSupport());
        AgentContext context = AgentContext.create(createRequest());

        assertThrows(IllegalStateException.class, () -> step.invoke(context));
    }

    private static OpenAiChatCompletionRequest createRequest() {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .build();
    }
}
