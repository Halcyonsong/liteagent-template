package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ChatAgentExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_agent_should_execute_and_return_response() {
        assumeConfigReady();

        OpenAiChatAgent agent = OpenAiChatAgents.create(
                HttpRuntimeConfig.builder()
                        .maxInMemorySize(properties.getRuntime().getMaxInMemorySize())
                        .connectTimeoutMillis(properties.getRuntime().getConnectTimeoutMillis())
                        .responseTimeoutMillis(properties.getRuntime().getResponseTimeoutMillis())
                        .build()
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一位精通法律的助手。"))
                .addMessage(Messages.user("你好，请简单介绍一下你自己。"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .build();

        OpenAiChatCompletionResponse response = agent.execute(request);
        Printers.printProviderResponse(response);
    }
}
