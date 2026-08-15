package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ProviderQuickRequestStreamExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiStreamClient streamClient;

    @Test
    void stream_by_quick_request_should_return_chunks() {
        assumeConfigReady();

        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .systemMessage("You are a helpful assistant. Answer concisely.")
                .userMessage("用三句话介绍一下 Java。")
                .build();

        System.out.println("===== QuickRequest Stream =====");
        streamClient.stream(request)
                .doOnNext(this::printDelta)
                .blockLast();
        System.out.println("\n===== Stream End =====");
    }

    private void printDelta(OpenAiStreamCompletionResponse response) {
        response.getChoices().forEach(choice -> {
            if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                System.out.print(choice.getDelta().getContent());
            }
        });
    }
}
