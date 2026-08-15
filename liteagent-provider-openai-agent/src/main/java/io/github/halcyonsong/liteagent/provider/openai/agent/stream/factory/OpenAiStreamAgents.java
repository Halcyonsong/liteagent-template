package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatStreamTransport;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

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

    public static OpenAiStreamAgent create(WebClient webClient,
                                           List<StreamStepHook> hooks,
                                           int maxStepCount) {
        Objects.requireNonNull(webClient, "webClient must not be null");

        OpenAiStreamAgentExecutorFactory factory = new OpenAiStreamAgentExecutorFactory(
                new OpenAiChatRequestMapper(),
                new OpenAiClientSupport(),
                new OpenAiChatStreamTransport(webClient),
                new OpenAiStreamResponseMapper()
        );

        return new OpenAiStreamAgent(factory.createAgent(hooks, maxStepCount));
    }

    public static OpenAiStreamAgent create(WebClientRegistry registry, HttpRuntimeConfig runtimeConfig) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");
        return create(registry.getOrCreateStreamClient(runtimeConfig));
    }

    public static OpenAiStreamAgent create(WebClientRegistry registry,
                                           HttpRuntimeConfig runtimeConfig,
                                           List<StreamStepHook> hooks,
                                           int maxStepCount) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");
        return create(registry.getOrCreateStreamClient(runtimeConfig), hooks, maxStepCount);
    }
}