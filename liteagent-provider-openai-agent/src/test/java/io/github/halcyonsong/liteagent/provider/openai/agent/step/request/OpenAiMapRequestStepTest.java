package io.github.halcyonsong.liteagent.provider.openai.agent.step.request;

import io.github.halcyonsong.liteagent.agent.context.AgentContext;
import io.github.halcyonsong.liteagent.agent.step.AgentStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiMapRequestStepTest {

    @Test
    void invoke_should_store_provider_request_and_raw_request() {
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

        AgentContext context = AgentContext.create(request);
        OpenAiMapRequestStep step = new OpenAiMapRequestStep(new OpenAiChatRequestMapper());

        AgentStepKey next = step.invoke(context);

        assertEquals(AgentStepKey.ENHANCE_REQUEST, next);
        assertSame(request, context.getAttribute(OpenAiAgentAttributes.PROVIDER_REQUEST));

        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );
        assertNotNull(rawRequest);
        assertEquals("test-model", rawRequest.getModel());
        assertEquals(1, rawRequest.getMessages().size());
    }
}
