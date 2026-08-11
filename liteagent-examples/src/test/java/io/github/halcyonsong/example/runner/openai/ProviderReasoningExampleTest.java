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
class ProviderReasoningExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiChatClient client;

    @Test
    void chat_should_print_reasoning_and_tool_calls_when_present() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("请回答一个需要推理的问题，并在支持时返回 reasoning_content。"))
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