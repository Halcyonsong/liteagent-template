package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OpenAI 同步 Agent 构造器，集中配置 HTTP 客户端来源、步骤 Hook 和执行限制。
 */
public final class OpenAiChatAgentBuilder {

    private static final int DEFAULT_MAX_STEP_COUNT = 1000;
    private static final int DEFAULT_MAX_ITERATIONS = 10;

    private WebClient webClient;
    private HttpRuntimeConfig runtimeConfig;
    private WebClientRegistry registry;
    private ToolExecutor toolExecutor;
    private final List<StepHook> hooks = new ArrayList<>();

    private int maxStepCount = DEFAULT_MAX_STEP_COUNT;
    private int maxIterations = DEFAULT_MAX_ITERATIONS;

    private OpenAiChatAgentBuilder() {
    }

    public static OpenAiChatAgentBuilder builder() {
        return new OpenAiChatAgentBuilder();
    }

    /**
     * 使用调用方自行管理生命周期的 WebClient。
     */
    public OpenAiChatAgentBuilder webClient(WebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient must not be null");
        return this;
    }

    /**
     * 使用运行时配置；未设置 registry 时使用框架共享的 registry。
     */
    public OpenAiChatAgentBuilder runtimeConfig(HttpRuntimeConfig runtimeConfig) {
        this.runtimeConfig = Objects.requireNonNull(
                runtimeConfig,
                "runtimeConfig must not be null"
        );
        return this;
    }

    /**
     * 指定调用方管理的 WebClientRegistry。
     * 必须同时调用 runtimeConfig(...)。
     */
    public OpenAiChatAgentBuilder registry(WebClientRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        return this;
    }

    /**
     * 指定自定义工具执行器，未设置时使用默认的 ReflectionToolExecutor。
     */
    public OpenAiChatAgentBuilder toolExecutor(ToolExecutor toolExecutor) {
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
        return this;
    }

    public OpenAiChatAgentBuilder addHook(StepHook hook) {
        this.hooks.add(Objects.requireNonNull(hook, "hook must not be null"));
        return this;
    }

    /**
     * 替换此前添加的全部 Hook。
     */
    public OpenAiChatAgentBuilder hooks(StepHook... hooks) {
        this.hooks.clear();
        if (hooks != null) {
            for (StepHook hook : hooks) {
                addHook(hook);
            }
        }
        return this;
    }

    /**
     * 替换此前添加的全部 Hook。
     */
    public OpenAiChatAgentBuilder hooks(List<? extends StepHook> hooks) {
        this.hooks.clear();
        if (hooks != null) {
            hooks.forEach(this::addHook);
        }
        return this;
    }

    public OpenAiChatAgentBuilder maxStepCount(int maxStepCount) {
        if (maxStepCount <= 0) {
            throw new IllegalArgumentException("maxStepCount must be positive");
        }
        this.maxStepCount = maxStepCount;
        return this;
    }

    public OpenAiChatAgentBuilder maxIterations(int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }
        this.maxIterations = maxIterations;
        return this;
    }

    public OpenAiChatAgent build() {
        List<StepHook> configuredHooks = List.copyOf(hooks);
        ToolExecutor executor = toolExecutor != null ? toolExecutor : new ReflectionToolExecutor();

        if (webClient != null) {
            if (runtimeConfig != null || registry != null) {
                throw new IllegalStateException(
                        "webClient cannot be combined with runtimeConfig or registry"
                );
            }
            return OpenAiChatAgents.create(
                    webClient,
                    configuredHooks,
                    maxStepCount,
                    maxIterations,
                    executor
            );
        }

        if (runtimeConfig == null) {
            throw new IllegalStateException(
                    "Either webClient or runtimeConfig must be configured"
            );
        }

        if (registry != null) {
            return OpenAiChatAgents.create(
                    registry,
                    runtimeConfig,
                    configuredHooks,
                    maxStepCount,
                    maxIterations,
                    executor
            );
        }

        return OpenAiChatAgents.create(
                runtimeConfig,
                configuredHooks,
                maxStepCount,
                maxIterations,
                executor
        );
    }
}