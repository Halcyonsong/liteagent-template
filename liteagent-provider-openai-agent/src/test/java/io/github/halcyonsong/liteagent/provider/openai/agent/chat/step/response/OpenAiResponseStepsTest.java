package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant.OpenAiChatAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiResponseStepsTest {

    @Test
    void map_chat_response_should_store_provider_response() {
        ChatAgentContext context = ChatAgentContext.create(createRequest());
        OpenAiChatCompletionRawResponse rawResponse = createRawResponse();

        context.setAttribute(OpenAiChatAgentAttributes.RAW_RESPONSE, rawResponse);
        OpenAiChatMapResponseStep step = new OpenAiChatMapResponseStep(new OpenAiChatResponseMapper());

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.ENHANCE_RESPONSE, next);
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiChatAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );
        assertNotNull(response);
        assertEquals("resp-1", response.getBaseResponse().getId());
        assertEquals(1, response.getChoices().size());
        assertEquals("hello", response.getChoices().get(0).getChatResponse().getMessages().get(0).getContent());
    }

    @Test
    void analyze_response_should_end_when_response_missing() {
        ChatAgentContext context = ChatAgentContext.create(createRequest());
        OpenAiChatAnalyzeResponseStep step = new OpenAiChatAnalyzeResponseStep();

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.END, next);
        assertEquals(AgentTerminationReason.MODEL_ERROR, context.getTerminationReason());
    }

    @Test
    void analyze_response_should_continue_when_response_exists() {
        ChatAgentContext context = ChatAgentContext.create(createRequest());
        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_RESPONSE, createProviderResponse());
        OpenAiChatAnalyzeResponseStep step = new OpenAiChatAnalyzeResponseStep();

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.BUILD_RESULT, next);
    }

    @Test
    void build_result_should_write_result_and_mark_completed() {
        ChatAgentContext context = ChatAgentContext.create(createRequest());
        OpenAiChatCompletionResponse response = createProviderResponse();
        context.setAttribute(OpenAiChatAgentAttributes.PROVIDER_RESPONSE, response);
        OpenAiChatBuildResultStep step = new OpenAiChatBuildResultStep();

        ChatStepKey next = step.invoke(context);

        assertEquals(ChatStepKey.END, next);
        assertSame(response, context.getResult());
        assertEquals(AgentTerminationReason.COMPLETED, context.getTerminationReason());
    }

    @Test
    void build_result_should_fail_when_provider_response_missing() {
        ChatAgentContext context = ChatAgentContext.create(createRequest());
        OpenAiChatBuildResultStep step = new OpenAiChatBuildResultStep();

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

    private static OpenAiChatCompletionRawResponse createRawResponse() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp-1");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(123L);
        rawResponse.setModel("test-model");

        OpenAiChatCompletionRawResponse.RawMessage rawMessage = new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("hello");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice = new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("stop");

        rawResponse.setChoices(List.of(rawChoice));
        return rawResponse;
    }

    private static OpenAiChatCompletionResponse createProviderResponse() {
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
