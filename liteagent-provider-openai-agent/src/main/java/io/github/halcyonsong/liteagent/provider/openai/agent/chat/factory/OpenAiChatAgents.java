package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI agent 自动装配入口。
 * <p>
 * 提供三类创建方式：
 * 1. 直接传入现成 WebClient
 * 2. 传入 HttpRuntimeConfig，使用默认共享 WebClientRegistry
 * 3. 传入自定义 WebClientRegistry + HttpRuntimeConfig
 */
public final class OpenAiChatAgents {

    /**
     * 默认共享的 WebClientFactory。
     */
    private static final WebClientFactory WEB_CLIENT_FACTORY = new WebClientFactory();

    /**
     * 默认共享的 WebClientRegistry。
     * <p>
     * 普通静态 create(HttpRuntimeConfig...) 入口会复用这里的缓存，
     * 避免每次重新创建底层 HTTP 客户端。
     */
    private static final WebClientRegistry WEB_CLIENT_REGISTRY = new WebClientRegistry(WEB_CLIENT_FACTORY);

    private OpenAiChatAgents() {
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent。
     * <p>
     * 适合调用方已经自行管理 WebClient 生命周期的场景。
     */
    public static OpenAiChatAgent create(WebClient webClient) {
        return create(webClient, List.of(), 1000);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入最大步骤数。
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount) {
        return create(webClient, List.of(), maxStepCount);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     */
    public static OpenAiChatAgent create(WebClient webClient, List<StepHook> hooks, int maxStepCount) {
        return createAgent(webClient, hooks, maxStepCount);
    }

    private static OpenAiChatAgent createAgent(WebClient webClient, List<StepHook> hooks, int maxStepCount) {
        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiClientSupport clientSupport = new OpenAiClientSupport();
        OpenAiChatTransport chatTransport = new OpenAiChatTransport(webClient);
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();

        OpenAiChatAgentExecutorFactory executorFactory =
                new OpenAiChatAgentExecutorFactory(
                        requestMapper,
                        clientSupport,
                        chatTransport,
                        responseMapper
                );

        ChatAgentExecutor executor =
                executorFactory.create(hooks, maxStepCount);

        return new OpenAiChatAgent(new ChatAgent(executor));
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent。
     * <p>
     * 使用默认共享 WebClientRegistry 复用 WebClient。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig) {
        return create(runtimeConfig, List.of(), 1000);
    }

    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig, int maxStepCount) {
        return create(runtimeConfig, List.of(), maxStepCount);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     * <p>
     * 该入口内部会走默认共享的 WebClientRegistry，不会重复创建 WebClient。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig, List<StepHook> hooks, int maxStepCount) {
        return create(WEB_CLIENT_REGISTRY, runtimeConfig, hooks, maxStepCount);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent。
     * <p>
     * 适合 Spring 等由外部管理 registry 生命周期的场景。
     */
    public static OpenAiChatAgent create(WebClientRegistry registry, HttpRuntimeConfig runtimeConfig) {
        return create(registry, runtimeConfig, List.of(), 1000);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，
     * 并允许传入 hook 与最大步骤数。
     */
    public static OpenAiChatAgent create(
            WebClientRegistry registry,
            HttpRuntimeConfig runtimeConfig,
            List<StepHook> hooks,
            int maxStepCount
    ) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");

        WebClient webClient =
                registry.getOrCreateChatClient(runtimeConfig);

        return create(webClient, hooks, maxStepCount);
    }
}