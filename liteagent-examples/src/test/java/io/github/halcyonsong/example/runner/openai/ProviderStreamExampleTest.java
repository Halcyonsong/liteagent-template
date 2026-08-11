package io.github.halcyonsong.example.runner.openai;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ProviderStreamExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiStreamClient client;

    @Test
    void stream_by_invocation_should_return_provider_stream_response() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，你是什么模型？"))
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .chatOptions(null)
                .build();

        client.streamCompletion(invocation)
                .doOnNext(Printers::printProviderStreamResponse)
                .blockLast();
    }
}