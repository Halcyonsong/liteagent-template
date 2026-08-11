package io.github.halcyonsong.liteagent.provider.openai.client.factory;

import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import org.springframework.web.reactive.function.client.WebClient;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;

import java.util.Objects;

/**
 * OpenAI-compatible 对话客户端工厂。
 * <p>
 * 负责通过运行时配置从 WebClient 注册表中获取基础 WebClient，
 * 并创建对应的 OpenAiChatClient。
 */
public class OpenAiChatClientFactory {

    private final WebClientRegistry webClientRegistry;

    public OpenAiChatClientFactory(WebClientRegistry webClientRegistry) {
        this.webClientRegistry = Objects.requireNonNull(webClientRegistry, "webClientRegistry must not be null");
    }

    public OpenAiChatClient create(HttpRuntimeConfig runtimeConfig) {
        WebClient webClient = webClientRegistry.getOrCreateChatClient(runtimeConfig);
        return new OpenAiChatClient(webClient);
    }

    public OpenAiStreamClient createStream(HttpRuntimeConfig runtimeConfig) {
        WebClient webClient = webClientRegistry.getOrCreateStreamClient(runtimeConfig);
        return new OpenAiStreamClient(webClient);
    }
}
