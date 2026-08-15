package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiChatResponseMapperTest {

    private final OpenAiChatResponseMapper mapper = new OpenAiChatResponseMapper();

    @Test
    void shouldMapRawResponseSuccessfully() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_1");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(123456L);
        rawResponse.setModel("gpt-test");

        OpenAiChatCompletionRawResponse.RawMessage rawMessage = new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("你好");
        rawMessage.setReasoningContent("我正在分析问题");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice = new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("stop");

        OpenAiChatCompletionRawResponse.RawUsage rawUsage = new OpenAiChatCompletionRawResponse.RawUsage();
        rawUsage.setPromptTokens(12);
        rawUsage.setCompletionTokens(8);
        rawUsage.setTotalTokens(20);

        rawResponse.setChoices(List.of(rawChoice));
        rawResponse.setUsage(rawUsage);

        OpenAiChatCompletionResponse response = mapper.fromRaw(rawResponse);

        assertEquals("resp_1", response.getBaseResponse().getId());
        assertEquals("chat.completion", response.getBaseResponse().getObject());
        assertEquals(123456L, response.getBaseResponse().getCreated());
        assertEquals("gpt-test", response.getBaseResponse().getModel());

        ChatChoice choice = response.getChoices().get(0);
        assertEquals(0, choice.getIndex());
        assertEquals(FinishReason.STOP, choice.getFinishReason());

        Message message = choice.getChatResponse().getMessages().get(0);
        assertInstanceOf(AssistantResponseMessage.class, message);

        AssistantResponseMessage assistantMessage = (AssistantResponseMessage) message;
        assertEquals("你好", assistantMessage.getContent());
        assertEquals("我正在分析问题", assistantMessage.getReasoningContent());
        assertTrue(assistantMessage.getToolCalls().isEmpty());

        assertEquals(12, response.getUsage().getPromptTokens());
        assertEquals(8, response.getUsage().getCompletionTokens());
        assertEquals(20, response.getUsage().getTotalTokens());
    }

    @Test
    void shouldMapToolCallsSuccessfully() {
        OpenAiChatCompletionRawResponse.RawFunction rawFunction = new OpenAiChatCompletionRawResponse.RawFunction();
        rawFunction.setName("get_weather");
        rawFunction.setArguments("{\"city\":\"北京\"}");

        OpenAiChatCompletionRawResponse.RawToolCall rawToolCall = new OpenAiChatCompletionRawResponse.RawToolCall();
        rawToolCall.setIndex(0);
        rawToolCall.setId("call_1");
        rawToolCall.setType("function");
        rawToolCall.setFunction(rawFunction);

        OpenAiChatCompletionRawResponse.RawMessage rawMessage = new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("");
        rawMessage.setReasoningContent("需要调用工具");
        rawMessage.setToolCalls(List.of(rawToolCall));

        OpenAiChatCompletionRawResponse.RawChoice rawChoice = new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("tool_calls");

        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_tool");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(1L);
        rawResponse.setModel("gpt-tool-test");
        rawResponse.setChoices(List.of(rawChoice));

        OpenAiChatCompletionResponse response = mapper.fromRaw(rawResponse);

        ChatChoice choice = response.getChoices().get(0);
        AssistantResponseMessage assistantMessage =
                (AssistantResponseMessage) choice.getChatResponse().getMessages().get(0);

        assertEquals(FinishReason.TOOL_CALLS, choice.getFinishReason());
        assertEquals(1, assistantMessage.getToolCalls().size());
        assertEquals("call_1", assistantMessage.getToolCalls().get(0).getId());
        assertEquals("get_weather", assistantMessage.getToolCalls().get(0).getFunction().getName());
    }

    @Test
    void shouldRejectEmptyChoices() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_empty");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(1L);
        rawResponse.setModel("gpt-test");
        rawResponse.setChoices(List.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mapper.fromRaw(rawResponse)
        );

        assertTrue(exception.getMessage().contains("choices must not be empty"));
    }
}
