package io.github.halcyonsong.example.runner.openai;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ProviderChatExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiChatClient client;

    @Test
    void chat_by_invocation_should_return_provider_response() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请简单介绍一下你自己。"))
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .chatOptions(null)
                .build();

        OpenAiChatCompletionResponse response = client.chatCompletion(invocation);
        Printers.printProviderResponse(response);
    }
}