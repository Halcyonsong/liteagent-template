package io.github.halcyonsong.example.runner.unified;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class InvocationStreamExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiStreamClient client;

    @Test
    void stream_by_invocation_should_return_stream_chunk() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请介绍一下你自己。"))
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

        client.stream(invocation)
                .doOnNext(Printers::printStreamChunk)
                .blockLast();
    }
}