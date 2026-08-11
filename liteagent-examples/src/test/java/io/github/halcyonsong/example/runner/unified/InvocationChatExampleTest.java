package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class InvocationChatExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiChatClient client;

    @Test
    void chat_by_invocation_should_return_chat_result() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(256)
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        ChatResult result = client.chat(invocation);
        Printers.printChatResult(result);
    }
}