package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class QuickRequestChatExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_by_quick_request_should_return_response() {
        assumeConfigReady();

        OpenAiChatAgent agent = OpenAiChatAgents.create(buildRuntimeConfig());

        OpenAiQuickChatRequest quick = OpenAiQuickChatRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .systemMessage("You are a helpful assistant.")
                .userMessage("你好，请简单介绍一下你自己。")
                .build();

        OpenAiChatCompletionRequest request = quick.toChatCompletion();

        OpenAiChatCompletionResponse response = agent.execute(request);
        Printers.printChatResponse(response);
    }
}
