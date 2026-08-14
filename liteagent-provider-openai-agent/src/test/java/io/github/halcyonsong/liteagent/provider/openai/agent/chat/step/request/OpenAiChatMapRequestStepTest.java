package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.request;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiChatMapRequestStepTest {

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

        ChatAgentContext context = ChatAgentContext.create(request);
        OpenAiChatMapRequestStep step = new OpenAiChatMapRequestStep(new OpenAiChatRequestMapper());

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.ENHANCE_REQUEST, next);
        assertSame(request, context.getAttribute(OpenAiChatAgentAttributes.PROVIDER_REQUEST));

        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiChatAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );
        assertNotNull(rawRequest);
        assertEquals("test-model", rawRequest.getModel());
        assertEquals(1, rawRequest.getMessages().size());
    }
}
