package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class StreamAgentExampleTest extends OpenAiExampleSupport {

    @Test
    void stream_agent_should_execute_and_return_chunks() {
        assumeConfigReady();

        WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());

        OpenAiStreamAgent agent = OpenAiStreamAgents.create(
                registry,
                HttpRuntimeConfig.builder()
                        .maxInMemorySize(properties.getRuntime().getMaxInMemorySize())
                        .connectTimeoutMillis(properties.getRuntime().getConnectTimeoutMillis())
                        .streamResponseTimeoutMillis(properties.getRuntime().getStreamResponseTimeoutMillis())
                        .build()
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .build();

        agent.execute(request)
                .doOnNext(Printers::printStreamDeltaContentAndReasoning)
                .blockLast();
    }
}
