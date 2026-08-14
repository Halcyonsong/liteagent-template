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
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI agent 自动装配入口。
 * <p>
 * 该类面向普通调用者提供最小装配成本的创建方式，
 * 同时保留基于 WebClient 与 HttpRuntimeConfig 的高级配置入口。
 */
public final class OpenAiChatAgents {

    private OpenAiChatAgents() {
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent。
     * <p>
     * 适合调用方已经自行管理 WebClient 生命周期的场景。
     *
     * @param webClient 已配置完成的 WebClient
     * @return 完成最小装配的 OpenAiChatAgent
     */
    public static OpenAiChatAgent create(WebClient webClient) {
        return create(webClient, List.of(), 1000);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入最大步骤数。
     *
     * @param webClient 已配置完成的 WebClient
     * @param maxStepCount 单次执行允许的最大步骤数
     * @return 完成装配的 OpenAiChatAgent
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount) {
        return create(webClient, List.of(), maxStepCount);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     *
     * @param webClient 已配置完成的 WebClient
     * @param hooks 步骤生命周期钩子
     * @param maxStepCount 单次执行允许的最大步骤数
     * @return 完成装配的 OpenAiChatAgent
     */
    public static OpenAiChatAgent create(WebClient webClient,
                                         List<StepHook> hooks,
                                         int maxStepCount) {
        Objects.requireNonNull(webClient, "webClient must not be null");

        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiClientSupport clientSupport = new OpenAiClientSupport();
        OpenAiChatTransport chatTransport = new OpenAiChatTransport(webClient);
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();

        OpenAiChatAgentExecutorFactory executorFactory = new OpenAiChatAgentExecutorFactory(
                requestMapper,
                clientSupport,
                chatTransport,
                responseMapper
        );

        ChatAgentExecutor executor = executorFactory.create(hooks, maxStepCount);
        ChatAgent chatAgent = new ChatAgent(executor);
        return new OpenAiChatAgent(chatAgent);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent。
     * <p>
     * 适合普通 Java 使用者只提供运行时参数，由框架内部自动创建 WebClient 的场景。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @return 完成最小装配的 OpenAiChatAgent
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig) {
        return create(runtimeConfig, List.of(), 1000);
    }

    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig, int maxStepCount) {
        return create(runtimeConfig, List.of(), maxStepCount);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @param hooks 步骤生命周期钩子
     * @param maxStepCount 单次执行允许的最大步骤数
     * @return 完成装配的 OpenAiChatAgent
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount) {
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");

        WebClientFactory webClientFactory = new WebClientFactory();
        WebClient webClient = webClientFactory.createChatClient(runtimeConfig);

        return create(webClient, hooks, maxStepCount);
    }
}