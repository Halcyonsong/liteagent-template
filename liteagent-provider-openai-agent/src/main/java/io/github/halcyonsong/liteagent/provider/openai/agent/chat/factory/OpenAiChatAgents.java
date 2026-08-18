package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.ChatAgent;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
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
 * OpenAI 同步 Agent 自动装配入口，支持传入现成 WebClient、HttpRuntimeConfig（共享 registry）或自定义 WebClientRegistry 三类创建方式。
 */
public final class OpenAiChatAgents {

    private OpenAiChatAgents() {
    }

    public static OpenAiChatAgentBuilder builder() {
        return OpenAiChatAgentBuilder.builder();
    }

    // ─── WebClient 入口 ───────────────────────────────────────────

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，适合调用方自行管理 WebClient 生命周期。
     */
    public static OpenAiChatAgent create(WebClient webClient) {
        return create(webClient, List.of(), 1000, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并指定最大步骤数。
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount) {
        return create(webClient, List.of(), maxStepCount, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并指定最大步骤数与最大迭代轮次。
     */
    public static OpenAiChatAgent create(WebClient webClient, int maxStepCount, int maxIterations) {
        return create(webClient, List.of(), maxStepCount, maxIterations);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     */
    public static OpenAiChatAgent create(WebClient webClient, List<StepHook> hooks, int maxStepCount) {
        return create(webClient, hooks, maxStepCount, 10);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，允许传入全部配置参数。
     */
    public static OpenAiChatAgent create(WebClient webClient,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations) {
        Objects.requireNonNull(webClient, "webClient must not be null");
        return createAgent(webClient, hooks, maxStepCount, maxIterations);
    }

    /**
     * 基于现成的 WebClient 创建 OpenAiChatAgent，允许传入全部配置参数及自定义工具执行器。
     */
    public static OpenAiChatAgent create(WebClient webClient,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations,
                                         ToolExecutor toolExecutor) {
        Objects.requireNonNull(webClient, "webClient must not be null");
        Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
        return createAgent(webClient, hooks, maxStepCount, maxIterations, toolExecutor);
    }

    // ─── HttpRuntimeConfig 入口（全局共享 registry）─────────────────

    /**
     * 基于运行时配置创建 OpenAiChatAgent，内部通过 sharedRegistry 复用 WebClient。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig) {
        return create(runtimeConfig, List.of(), 1000, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并指定最大步骤数。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig, int maxStepCount) {
        return create(runtimeConfig, List.of(), maxStepCount, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并指定最大步骤数与最大迭代轮次。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         int maxStepCount,
                                         int maxIterations) {
        return create(runtimeConfig, List.of(), maxStepCount, maxIterations);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount) {
        return create(runtimeConfig, hooks, maxStepCount, 10);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，允许传入全部配置参数。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations) {
        return create(OpenAiRuntime.sharedRegistry(), runtimeConfig, hooks, maxStepCount, maxIterations);
    }

    /**
     * 基于运行时配置创建 OpenAiChatAgent，并使用自定义工具执行器。
     */
    public static OpenAiChatAgent create(HttpRuntimeConfig runtimeConfig,
                                         List<StepHook> hooks,
                                         int maxStepCount,
                                         int maxIterations,
                                         ToolExecutor toolExecutor) {
        return create(OpenAiRuntime.sharedRegistry(), runtimeConfig, hooks, maxStepCount, maxIterations, toolExecutor);
    }

    // ─── 自定义 WebClientRegistry 入口 ────────────────────────────

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，适合 Spring 等外部管理 registry 的场景。
     */
    public static OpenAiChatAgent create(WebClientRegistry registry, HttpRuntimeConfig runtimeConfig) {
        return create(registry, runtimeConfig, List.of(), 1000, 10);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，并指定最大步骤数。
     */
    public static OpenAiChatAgent create(WebClientRegistry registry,
                                         HttpRuntimeConfig runtimeConfig,
                                         int maxStepCount) {
        return create(registry, runtimeConfig, List.of(), maxStepCount, 10);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，并指定最大步骤数与最大迭代轮次。
     */
    public static OpenAiChatAgent create(WebClientRegistry registry,
                                         HttpRuntimeConfig runtimeConfig,
                                         int maxStepCount,
                                         int maxIterations) {
        return create(registry, runtimeConfig, List.of(), maxStepCount, maxIterations);
    }

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，并允许传入 hook 与最大步骤数。
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
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，允许传入全部配置参数。
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

    /**
     * 基于自定义 WebClientRegistry 和运行时配置创建 OpenAiChatAgent，并使用自定义工具执行器。
     */
    public static OpenAiChatAgent create(
            WebClientRegistry registry,
            HttpRuntimeConfig runtimeConfig,
            List<StepHook> hooks,
            int maxStepCount,
            int maxIterations,
            ToolExecutor toolExecutor
    ) {
        Objects.requireNonNull(registry, "registry must not be null");
        Objects.requireNonNull(runtimeConfig, "runtimeConfig must not be null");
        Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");

        return create(registry.getOrCreateChatClient(runtimeConfig), hooks, maxStepCount, maxIterations, toolExecutor);
    }

    // ─── 内部实现 ─────────────────────────────────────────────────

    /**
     * 组装默认 OpenAI chat 组件并创建 agent。
     */
    private static OpenAiChatAgent createAgent(WebClient webClient,
                                               List<StepHook> hooks,
                                               int maxStepCount,
                                               int maxIterations) {
        return createAgent(webClient, hooks, maxStepCount, maxIterations, null);
    }

    private static OpenAiChatAgent createAgent(WebClient webClient,
                                               List<StepHook> hooks,
                                               int maxStepCount,
                                               int maxIterations,
                                               ToolExecutor toolExecutor) {
        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiAdvisorsExecutor clientSupport = new OpenAiAdvisorsExecutor();
        OpenAiChatTransport chatTransport = new OpenAiChatTransport(webClient);
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();

        OpenAiChatAgentExecutorFactory executorFactory = (toolExecutor != null)
                ? new OpenAiChatAgentExecutorFactory(requestMapper, clientSupport, chatTransport, responseMapper, toolExecutor)
                : new OpenAiChatAgentExecutorFactory(requestMapper, clientSupport, chatTransport, responseMapper);

        ChatAgentExecutor executor = executorFactory.create(hooks, maxStepCount, maxIterations);

        return new OpenAiChatAgent(new ChatAgent(executor));
    }
}