package io.github.halcyonsong.liteagent.provider.openai.request.advisor;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiToolChoiceAdvisorTest {

    @Test
    void should_enhance_raw_request_with_tool_choice_function() {
        OpenAiToolChoiceAdvisor advisor = new OpenAiToolChoiceAdvisor(OpenAiToolChoice.function("get_weather"));
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        advisor.enhance(dummyRequest(), rawRequest);

        assertNotNull(rawRequest.getToolChoice());
        assertTrue(rawRequest.getToolChoice() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> toolChoice = (Map<String, Object>) rawRequest.getToolChoice();
        assertEquals("function", toolChoice.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> function = (Map<String, Object>) toolChoice.get("function");
        assertEquals("get_weather", function.get("name"));
    }

    @Test
    void should_enhance_raw_request_with_tool_choice_none() {
        OpenAiToolChoiceAdvisor advisor = new OpenAiToolChoiceAdvisor(OpenAiToolChoice.none());
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        advisor.enhance(dummyRequest(), rawRequest);

        assertEquals("none", rawRequest.getToolChoice());
    }

    private OpenAiChatCompletionRequest dummyRequest() {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("test-key")
                        .model("gpt-test")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .completionOptions(OpenAiCompletionOptions.builder().build())
                .build();
    }
}