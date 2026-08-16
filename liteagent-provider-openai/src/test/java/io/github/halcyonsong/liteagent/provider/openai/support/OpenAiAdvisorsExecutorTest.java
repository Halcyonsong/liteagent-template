package io.github.halcyonsong.liteagent.provider.openai.support;

import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiAdvisorsExecutorTest {

    private final OpenAiAdvisorsExecutor support = new OpenAiAdvisorsExecutor();

    @Test
    void apply_advisors_should_run_all_advisors_in_order() {
        List<String> calls = new ArrayList<>();
        OpenAiChatCompletionRequest request = createRequest(
                List.of(
                        namedAdvisor("a1", calls, 0.1),
                        namedAdvisor("a2", calls, 0.2)
                )
        );
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        support.applyRequestAdvisors(request, rawRequest);

        assertEquals(List.of("a1", "a2"), calls);
        assertEquals(0.2, rawRequest.getTemperature());
    }

    @Test
    void apply_advisors_should_do_nothing_when_advisor_list_is_empty() {
        OpenAiChatCompletionRequest request = createRequest(List.of());
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        support.applyRequestAdvisors(request, rawRequest);

        assertNull(rawRequest.getTemperature());
    }

    private static RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest> namedAdvisor(
            String name,
            List<String> calls,
            double temperature
    ) {
        return (request, rawRequest) -> {
            calls.add(name);
            rawRequest.setTemperature(temperature);
        };
    }

    private static OpenAiChatCompletionRequest createRequest(
            List<RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest>> advisors
    ) {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .requestAdvisors(advisors)
                .build();
    }
}
