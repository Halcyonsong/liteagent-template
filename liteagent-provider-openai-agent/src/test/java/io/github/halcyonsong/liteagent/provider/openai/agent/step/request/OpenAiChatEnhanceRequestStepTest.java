package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiChatEnhanceRequestStepTest {

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
        ChatAgentContext context = ChatAgentContext.create(request);
        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_REQUEST, request);
        context.setAttribute(OpenAiChatAgentAttributes.RAW_REQUEST, rawRequest);

        OpenAiChatEnhanceRequestStep step = new OpenAiChatEnhanceRequestStep(new OpenAiClientSupport());

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.SEND_REQUEST, next);
        assertEquals(0.5, rawRequest.getTemperature());
        assertEquals(Boolean.FALSE, rawRequest.getStream());
    }

    @Test
    void invoke_should_fail_when_context_missing_required_attributes() {
        OpenAiChatEnhanceRequestStep step = new OpenAiChatEnhanceRequestStep(new OpenAiClientSupport());
        ChatAgentContext context = ChatAgentContext.create(createRequest());

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
