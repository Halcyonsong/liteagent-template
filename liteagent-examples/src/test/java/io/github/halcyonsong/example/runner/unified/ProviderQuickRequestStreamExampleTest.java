package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest.OpenAiQuickChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * QuickRequest 流式示例：通过 {@code OpenAiQuickChatRequest} 构建请求，流式输出。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class ProviderQuickRequestStreamExampleTest extends OpenAiExampleSupport {

    @Test
    void stream_by_quick_request_should_return_chunks() {
        assumeConfigReady();

        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .systemMessage("You are a helpful assistant. Answer concisely.")
                .userMessage("用三句话介绍一下 Java。")
                .build();

        OpenAiChatCompletionRequest request = quick.toChatCompletion();

        System.out.println("===== QuickRequest Stream =====");
        streamAgent.execute(request)
                .doOnNext(Printers::printStreamDeltaAll)
                .blockLast();
        System.out.println("\n===== Stream End =====");
    }
}
