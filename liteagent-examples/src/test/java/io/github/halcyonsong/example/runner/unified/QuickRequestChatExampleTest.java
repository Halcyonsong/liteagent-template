package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * QuickRequest 示例：通过 {@code OpenAiQuickChatRequest} 一行构建请求，省去手动组装 ChatRequest。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class QuickRequestChatExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_by_quick_request_should_return_response() {
        assumeConfigReady();

        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .systemMessage("You are a helpful assistant.")
                .userMessage("你好，请简单介绍一下你自己。")
                .build();

        OpenAiChatCompletionRequest request = quick.toChatCompletion();

        OpenAiChatCompletionResponse response = chatAgent.execute(request);
        Printers.printChatResponse(response);
    }
}
