package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.OpenAiRuntime;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI 同步 Agent 自动装配入口。
 * <p>
 * 提供三类创建方式，按由简到繁排列：
 * <ol>
 *     <li>传入现成 {@link WebClient} — 调用方完全自行管理 HTTP 客户端生命周期</li>
 *     <li>传入 {@link HttpRuntimeConfig} — 通过 {@link OpenAiRuntime} 全局共享 registry 复用 WebClient</li>
 *     <li>传入自定义 {@link WebClientRegistry} + {@link HttpRuntimeConfig} — 适合 Spring 等外部管理 registry 的场景</li>
 * </ol>
 */
public final class OpenAiChatAgents {

    private OpenAiChatAgents() {
    }

    // ─── WebClient 入口 ───────────────────────────────────────────

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent。
     * <p>
     * 适合调用方已经自行管理 WebClient 生命周期的场景。
     *
     * @param webClient 已构建好的 WebClient 实例
     */
    public static OpenAiChatAgent create(WebClient webClient) {
        return create(webClient, List.of(), 1000, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并指定最大步骤数。
     *
     * @param webClient    已构建好的 WebClient 实例
     * @param maxStepCount agent 编排最大步骤数（防止无限循环）
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount) {
        return create(webClient, List.of(), maxStepCount, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并指定最大步骤数与最大迭代轮次。
     *
     * @param webClient     已构建好的 WebClient 实例
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     * @param maxIterations 最大模型调用轮次（防止无限工具调用循环）
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount, int maxIterations) {
        return create(webClient, List.of(), maxStepCount, maxIterations);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     *
     * @param webClient    已构建好的 WebClient 实例
     * @param hooks        步骤钩子列表
     * @param maxStepCount agent 编排最大步骤数（防止无限循环）
     */
    public static OpenAiChatAgent create(WebClient webClient, List<StepHook> hooks, int maxStepCount) {
        return create(webClient, hooks, maxStepCount, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，允许传入全部配置参数。
     *
     * @param webClient     已构建好的 WebClient 实例
     * @param hooks         步骤钩子列表
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     * @param maxIterations 最大模型调用轮次（防止无限工具调用循环）
     */
    public static OpenAiChatAgent create(WebClient webClient,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations) {
        Objects.requireNonNull(webClient, "webClient must not be null");
        return createAgent(webClient, hooks, maxStepCount, maxIterations);
    }

    // ─── HttpRuntimeConfig 入口（全局共享 registry）─────────────────

    /**
     * 基于运行时配置创建 OpenAiChatAgent。
     * <p>
     * 内部通过 {@link OpenAiRuntime#sharedRegistry()} 复用 WebClient，
     * 相同配置的多次调用不会重复创建 HTTP 客户端。
     *
     * @param runtimeConfig HTTP 运行时配置
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig) {
        return create(runtimeConfig, List.of(), 1000, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并指定最大步骤数。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig, int maxStepCount) {
        return create(runtimeConfig, List.of(), maxStepCount, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并指定最大步骤数与最大迭代轮次。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     * @param maxIterations 最大模型调用轮次（防止无限工具调用循环）
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         int maxStepCount,
                                         int maxIterations) {
        return create(runtimeConfig, List.of(), maxStepCount, maxIterations);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     * <p>
     * 内部通过 {@link OpenAiRuntime#sharedRegistry()} 复用 WebClient。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @param hooks         步骤钩子列表
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount) {
        return create(runtimeConfig, hooks, maxStepCount, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，允许传入全部配置参数。
     * <p>
     * 内部通过 {@link OpenAiRuntime#sharedRegistry()} 复用 WebClient。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @param hooks         步骤钩子列表
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     * @param maxIterations 最大模型调用轮次（防止无限工具调用循环）
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations) {
        return create(OpenAiRuntime.sharedRegistry(), runtimeConfig, hooks, maxStepCount, maxIterations);
    }

    // ─── 自定义 WebClientRegistry 入口 ────────────────────────────

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent。
     * <p>
     * 适合 Spring 等由外部管理 registry 生命周期的场景。
     *
     * @param registry      自定义的 WebClientRegistry 实例
     * @param runtimeConfig HTTP 运行时配置
     */
    public static OpenAiChatAgent create(WebClientRegistry registry, HttpRuntimeConfig runtimeConfig) {
        return create(registry, runtimeConfig, List.of(), 1000, 10);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，
     * 并允许传入 hook 与最大步骤数。
     *
     * @param registry      自定义的 WebClientRegistry 实例
     * @param runtimeConfig HTTP 运行时配置
     * @param hooks         步骤钩子列表
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     */
    public static OpenAiChatAgent create(
            WebClientRegistry registry,
            HttpRuntimeConfig runtimeConfig,
            List<StepHook> hooks,
            int maxStepCount
    ) {
        return create(registry, runtimeConfig, hooks, maxStepCount, 10);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，
     * 允许传入全部配置参数。
     *
     * @param registry      自定义的 WebClientRegistry 实例
     * @param runtimeConfig HTTP 运行时配置
     * @param hooks         步骤钩子列表
     * @param maxStepCount  agent 编排最大步骤数（防止无限循环）
     * @param maxIterations 最大模型调用轮次（防止无限工具调用循环）
     */
    public static OpenAiChatAgent create(
            WebClientRegistry registry,
            HttpRuntimeConfig runtimeConfig,
            List<StepHook> hooks,
            int maxStepCount,
            int maxIterations
    ) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");

        WebClient webClient = registry.getOrCreateChatClient(runtimeConfig);

        return create(webClient, hooks, maxStepCount, maxIterations);
    }

    // ─── 内部实现 ─────────────────────────────────────────────────

    /**
     * 组装默认 OpenAI chat 组件并创建 agent。
     */
    private static OpenAiChatAgent createAgent(WebClient webClient,
                                               List<StepHook> hooks,
                                               int maxStepCount,
                                               int maxIterations) {
        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiAdvisorsExecutor clientSupport = new OpenAiAdvisorsExecutor();
        OpenAiChatTransport chatTransport = new OpenAiChatTransport(webClient);
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();

        OpenAiChatAgentExecutorFactory executorFactory =
                new OpenAiChatAgentExecutorFactory(
                        requestMapper,
                        clientSupport,
                        chatTransport,
                        responseMapper
                );

        ChatAgentExecutor executor = executorFactory.create(hooks, maxStepCount, maxIterations);

        return new OpenAiChatAgent(new ChatAgent(executor));
    }
}