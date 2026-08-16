package io.github.halcyonsong.liteagent.provider.openai.agent.stream.state;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamRoundAccumulatorTest {

    @Test
    void accumulate_should_concatenate_content_across_chunks() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, "Hello", null, null, null));
        acc.accumulate(chunk(null, 0, ", ", null, null, null));
        acc.accumulate(chunk(null, 0, "World!", null, null, null));

        List<StreamChoice> choices = acc.getChoices();
        assertEquals(1, choices.size());
        assertEquals("Hello, World!", choices.get(0).getDelta().getContent());
    }

    @Test
    void accumulate_should_concatenate_reasoning_content() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, null, "Thinking", null, null));
        acc.accumulate(chunk(null, 0, null, " about", null, null));
        acc.accumulate(chunk(null, 0, null, " it", null, null));

        assertEquals("Thinking about it", acc.getChoices().get(0).getDelta().getReasoningContent());
    }

    @Test
    void accumulate_should_assemble_tool_call_fragments_by_index() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, null, null, List.of(
                new ToolCall(0, "call_1", "function", new FunctionCall("get_weather", ""))
        ), null));
        acc.accumulate(chunk(null, 0, null, null, List.of(
                new ToolCall(0, null, null, new FunctionCall(null, "{\"city\""))
        ), null));
        acc.accumulate(chunk(null, 0, null, null, List.of(
                new ToolCall(0, null, null, new FunctionCall(null, ":\"北京\"}"))
        ), null));

        List<ToolCall> toolCalls = acc.getChoices().get(0).getDelta().getToolCalls();
        assertEquals(1, toolCalls.size());
        assertEquals("call_1", toolCalls.get(0).getId());
        assertEquals("function", toolCalls.get(0).getType());
        assertEquals("get_weather", toolCalls.get(0).getFunction().getName());
        assertEquals("{\"city\":\"北京\"}", toolCalls.get(0).getFunction().getArguments());
    }

    @Test
    void accumulate_should_handle_multiple_tool_calls() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, null, null, List.of(
                new ToolCall(0, "call_1", "function", new FunctionCall("get_weather", "{}")),
                new ToolCall(1, "call_2", "function", new FunctionCall("get_time", "{}"))
        ), null));

        List<ToolCall> toolCalls = acc.getChoices().get(0).getDelta().getToolCalls();
        assertEquals(2, toolCalls.size());
        assertEquals("get_weather", toolCalls.get(0).getFunction().getName());
        assertEquals("get_time", toolCalls.get(1).getFunction().getName());
    }

    @Test
    void accumulate_should_handle_multiple_choices() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, "Hello", null, null, null));
        acc.accumulate(chunk(null, 1, "Hi", null, null, null));

        List<StreamChoice> choices = acc.getChoices();
        assertEquals(2, choices.size());
        assertEquals("Hello", choices.get(0).getDelta().getContent());
        assertEquals("Hi", choices.get(1).getDelta().getContent());
    }

    @Test
    void accumulate_should_track_finish_reason() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        acc.accumulate(chunk(null, 0, "text", null, null, null));
        acc.accumulate(chunk(null, 0, null, null, null, FinishReason.STOP));

        assertEquals(FinishReason.STOP, acc.getFinishReason());
        assertEquals(FinishReason.STOP, acc.getChoices().get(0).getFinishReason());
    }

    @Test
    void accumulate_should_track_usage() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        OpenAiUsage usage = new OpenAiUsage(10, 5, 15, null, null, 10, 10);

        acc.accumulate(chunk(null, 0, "text", null, null, null));
        acc.accumulate(chunkWithUsage(usage));

        assertNotNull(acc.getUsage());
        assertEquals(15, acc.getUsage().getTotalTokens());
    }

    @Test
    void accumulate_should_track_base_response() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        OpenAiBaseResponse base = new OpenAiBaseResponse("id_1", "chat.completion.chunk", 123L, "gpt-4");

        acc.accumulate(chunk(base, 0, "text", null, null, null));

        assertTrue(acc.hasBaseResponse());
        assertEquals("id_1", acc.getBaseResponse().getId());
    }

    @Test
    void accumulate_should_ignore_null_chunk() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        acc.accumulate(null);
        assertFalse(acc.hasData());
    }

    @Test
   void accumulate_should_ignore_empty_choices() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        acc.accumulate(new OpenAiStreamCompletionResponse(
                new OpenAiBaseResponse("id-empty", "chat.completion.chunk", 1L, "gpt-4"),
                List.of(),
                null
        ));
        assertFalse(acc.hasChoices());
    }

    @Test
    void tryToFinalResponse_should_return_null_without_base_response() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        acc.accumulate(chunk(null, 0, "text", null, null, null));
        assertNull(acc.tryToFinalResponse());
    }

    @Test
    void tryToFinalResponse_should_build_response_with_base_response() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        OpenAiBaseResponse base = new OpenAiBaseResponse("id_1", "chat.completion.chunk", 1L, "gpt-4");

        acc.accumulate(chunk(base, 0, "Hello", null, null, null));
        acc.accumulate(chunk(null, 0, " World", null, null, FinishReason.STOP));

        OpenAiStreamCompletionResponse finalResp = acc.tryToFinalResponse();
        assertNotNull(finalResp);
        assertEquals("id_1", finalResp.getBaseResponse().getId());
        assertEquals("Hello World", finalResp.getChoices().get(0).getDelta().getContent());
        assertEquals(FinishReason.STOP, finalResp.getChoices().get(0).getFinishReason());
    }

    @Test
    void toFinalResponse_should_throw_without_base_response() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();
        assertThrows(IllegalStateException.class, acc::toFinalResponse);
    }

    @Test
    void hasData_should_be_true_with_any_data() {
        OpenAiStreamRoundAccumulator acc = new OpenAiStreamRoundAccumulator();

        assertFalse(acc.hasData());

        acc.accumulate(chunk(null, 0, "text", null, null, null));
        assertTrue(acc.hasData());
    }

    private static OpenAiStreamCompletionResponse chunk(
            OpenAiBaseResponse baseResponse,
            int choiceIndex,
            String content,
            String reasoningContent,
            List<ToolCall> toolCalls,
            FinishReason finishReason
    ) {
        StreamDelta delta = new StreamDelta(null, content, reasoningContent, toolCalls);
        StreamChoice choice = new StreamChoice(choiceIndex, delta, finishReason);
        return new OpenAiStreamCompletionResponse(baseResponse, List.of(choice), null);
    }

    private static OpenAiStreamCompletionResponse chunkWithUsage(OpenAiUsage usage) {
        return new OpenAiStreamCompletionResponse(
                new OpenAiBaseResponse("id-usage", "chat.completion.chunk", 1L, "gpt-4"),
                List.of(),
                usage
        );
    }
}
