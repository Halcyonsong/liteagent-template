package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiChatResponseMapperTest {

    @Test
    void from_raw_should_map_reasoning_and_tool_calls() {
        OpenAiChatCompletionRawResponse.RawFunction rawFunction =
                new OpenAiChatCompletionRawResponse.RawFunction();
        rawFunction.setName("get_weather");
        rawFunction.setArguments("{\"city\":\"北京\"}");

        OpenAiChatCompletionRawResponse.RawToolCall rawToolCall =
                new OpenAiChatCompletionRawResponse.RawToolCall();
        rawToolCall.setIndex(0);
        rawToolCall.setId("call_1");
        rawToolCall.setType("function");
        rawToolCall.setFunction(rawFunction);

        OpenAiChatCompletionRawResponse.RawMessage rawMessage =
                new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("");
        rawMessage.setReasoningContent("思考内容");
        rawMessage.setToolCalls(List.of(rawToolCall));

        OpenAiChatCompletionRawResponse.RawChoice rawChoice =
                new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("tool_calls");

        OpenAiChatCompletionRawResponse.RawUsage rawUsage =
                new OpenAiChatCompletionRawResponse.RawUsage();
        rawUsage.setPromptTokens(10);
        rawUsage.setCompletionTokens(20);
        rawUsage.setTotalTokens(30);

        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_1");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(123456L);
        rawResponse.setModel("test-model");
        rawResponse.setChoices(List.of(rawChoice));
        rawResponse.setUsage(rawUsage);

        OpenAiChatResponseMapper mapper = new OpenAiChatResponseMapper();
        OpenAiChatCompletionResponse response = mapper.fromRaw(rawResponse);

        assertEquals("resp_1", response.getBaseResponse().getId());
        assertEquals("chat.completion", response.getBaseResponse().getObject());
        assertEquals(123456L, response.getBaseResponse().getCreated());
        assertEquals("test-model", response.getBaseResponse().getModel());

        assertEquals(30, response.getUsage().getTotalTokens());

        ChatChoice choice = response.getChoices().get(0);
        assertEquals(0, choice.getIndex());
        assertEquals(FinishReason.TOOL_CALLS, choice.getFinishReason());

        assertEquals(1, choice.getChatResponse().getMessages().size());
        assertTrue(choice.getChatResponse().getMessages().get(0) instanceof OpenAiAssistantMessage);

        OpenAiAssistantMessage message =
                (OpenAiAssistantMessage) choice.getChatResponse().getMessages().get(0);

        assertEquals("", message.getContent());
        assertEquals("思考内容", message.getReasoningContent());
        assertEquals(1, message.getToolCalls().size());
        assertEquals("call_1", message.getToolCalls().get(0).getId());
        assertEquals("function", message.getToolCalls().get(0).getType());
        assertEquals("get_weather", message.getToolCalls().get(0).getFunction().getName());
        assertEquals("{\"city\":\"北京\"}", message.getToolCalls().get(0).getFunction().getArguments());
    }

    @Test
    void from_raw_should_map_stop_finish_reason() {
        OpenAiChatCompletionRawResponse.RawMessage rawMessage =
                new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("你好");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice =
                new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("stop");

        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_2");
        rawResponse.setObject("chat.completion");
        rawResponse.setModel("test-model");
        rawResponse.setChoices(List.of(rawChoice));

        OpenAiChatResponseMapper mapper = new OpenAiChatResponseMapper();
        OpenAiChatCompletionResponse response = mapper.fromRaw(rawResponse);

        ChatChoice choice = response.getChoices().get(0);
        assertEquals(FinishReason.STOP, choice.getFinishReason());

        OpenAiAssistantMessage message =
                (OpenAiAssistantMessage) choice.getChatResponse().getMessages().get(0);
        assertEquals("你好", message.getContent());
        assertNull(message.getReasoningContent());
        assertTrue(message.getToolCalls().isEmpty());
    }

    @Test
    void to_chat_result_should_downgrade_provider_message_to_core_message() {
        OpenAiChatCompletionRawResponse.RawMessage rawMessage =
                new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("最终回答");
        rawMessage.setReasoningContent("思考过程");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice =
                new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("stop");

        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_3");
        rawResponse.setObject("chat.completion");
        rawResponse.setModel("test-model");
        rawResponse.setChoices(List.of(rawChoice));

        OpenAiChatResponseMapper mapper = new OpenAiChatResponseMapper();
        OpenAiChatCompletionResponse response = mapper.fromRaw(rawResponse);

        ChatResult result = response.toChatResult();

        assertEquals(1, result.getChoices().size());
        assertTrue(result.getChoices().get(0).getChatResponse().getMessages().get(0) instanceof AssistantMessage);
        assertFalse(result.getChoices().get(0).getChatResponse().getMessages().get(0) instanceof OpenAiAssistantMessage);
        assertEquals("最终回答",
                result.getChoices().get(0).getChatResponse().getMessages().get(0).getContent());
    }

    @Test
    void from_raw_should_fail_when_choices_empty() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setChoices(List.of());

        OpenAiChatResponseMapper mapper = new OpenAiChatResponseMapper();

        assertThrows(IllegalStateException.class, () -> mapper.fromRaw(rawResponse));
    }
}