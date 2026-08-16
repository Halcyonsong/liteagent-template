package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamResponseMapperTest {

    private final OpenAiStreamResponseMapper mapper = new OpenAiStreamResponseMapper();

    @Test
    void shouldMapRawStreamResponseSuccessfully() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_stream_1");
        rawResponse.setObject("chat.completion.chunk");
        rawResponse.setCreated(123456L);
        rawResponse.setModel("gpt-stream-test");

        OpenAiChatCompletionRawResponse.RawMessage delta = new OpenAiChatCompletionRawResponse.RawMessage();
        delta.setRole("assistant");
        delta.setContent("你好");
        delta.setReasoningContent("正在思考");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice = new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setDelta(delta);
        rawChoice.setFinishReason("stop");

        OpenAiChatCompletionRawResponse.RawUsage rawUsage = new OpenAiChatCompletionRawResponse.RawUsage();
        rawUsage.setPromptTokens(20);
        rawUsage.setCompletionTokens(8);
        rawUsage.setTotalTokens(28);

        rawResponse.setChoices(List.of(rawChoice));
        rawResponse.setUsage(rawUsage);

        OpenAiStreamCompletionResponse response = mapper.fromRaw(rawResponse);

        assertEquals("resp_stream_1", response.getBaseResponse().getId());
        assertEquals("chat.completion.chunk", response.getBaseResponse().getObject());
        assertEquals(123456L, response.getBaseResponse().getCreated());
        assertEquals("gpt-stream-test", response.getBaseResponse().getModel());

        assertEquals(1, response.getChoices().size());
        StreamChoice choice = response.getChoices().get(0);
        assertEquals(0, choice.getIndex());
        assertEquals(FinishReason.STOP, choice.getFinishReason());
        assertEquals("assistant", choice.getDelta().getRole());
        assertEquals("你好", choice.getDelta().getContent());
        assertEquals("正在思考", choice.getDelta().getReasoningContent());

        assertEquals(20, response.getUsage().getPromptTokens());
        assertEquals(8, response.getUsage().getCompletionTokens());
        assertEquals(28, response.getUsage().getTotalTokens());
    }

    @Test
    void shouldReturnEmptyChoicesWhenRawChoicesEmpty() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_empty");
        rawResponse.setObject("chat.completion.chunk");
        rawResponse.setCreated(1L);
        rawResponse.setModel("test-model");
        rawResponse.setChoices(List.of());

        OpenAiStreamCompletionResponse response = mapper.fromRaw(rawResponse);

        assertNotNull(response);
        assertTrue(response.getChoices().isEmpty());
    }

    @Test
    void shouldMapNullDeltaToEmptyStreamDelta() {
        OpenAiChatCompletionRawResponse.RawChoice rawChoice = new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setDelta(null);
        rawChoice.setFinishReason(null);

        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp_null_delta");
        rawResponse.setObject("chat.completion.chunk");
        rawResponse.setCreated(1L);
        rawResponse.setModel("test-model");
        rawResponse.setChoices(List.of(rawChoice));

        OpenAiStreamCompletionResponse response = mapper.fromRaw(rawResponse);

        assertEquals(1, response.getChoices().size());
        StreamChoice choice = response.getChoices().get(0);
        assertNull(choice.getDelta().getRole());
        assertNull(choice.getDelta().getContent());
        assertNull(choice.getDelta().getReasoningContent());
        assertEquals(null, choice.getFinishReason());
    }
}