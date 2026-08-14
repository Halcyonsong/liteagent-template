package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatStreamTransport;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * OpenAI-compatible 流式 agent 快速装配入口。
 */
public final class OpenAiStreamAgents {

    private OpenAiStreamAgents() {
    }

    public static OpenAiStreamAgent create(WebClient webClient) {
        Objects.requireNonNull(webClient, "webClient must not be null");

        OpenAiStreamAgentExecutorFactory factory = new OpenAiStreamAgentExecutorFactory(
                new OpenAiChatRequestMapper(),
                new OpenAiClientSupport(),
                new OpenAiChatStreamTransport(webClient),
                new OpenAiStreamResponseMapper()
        );

        return new OpenAiStreamAgent(factory.createAgent());
    }

    public static OpenAiStreamAgent create(WebClientRegistry registry, HttpRuntimeConfig runtimeConfig) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");
        return create(registry.getOrCreateStreamClient(runtimeConfig));
    }
}