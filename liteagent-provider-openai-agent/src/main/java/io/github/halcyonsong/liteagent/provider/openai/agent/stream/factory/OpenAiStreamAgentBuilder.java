package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OpenAI 流式 Agent 构造器。
 * <p>
 * 用于集中配置 HTTP 客户端来源、步骤 Hook 和执行限制。
 * Agent 实际组装委托给 {@link OpenAiStreamAgents}。
 */
public final class OpenAiStreamAgentBuilder {

    private static final int DEFAULT_MAX_STEP_COUNT = 1000;
    private static final int DEFAULT_MAX_ITERATIONS = 10;

    private WebClient webClient;
    private HttpRuntimeConfig runtimeConfig;
    private WebClientRegistry registry;
    private ToolExecutor toolExecutor;
    private final List<StreamStepHook> hooks = new ArrayList<>();

    private int maxStepCount = DEFAULT_MAX_STEP_COUNT;
    private int maxIterations = DEFAULT_MAX_ITERATIONS;

    private OpenAiStreamAgentBuilder() {
    }

    /**
     * 创建构造器。
     *
     * @return 新的构造器实例
     */
    public static OpenAiStreamAgentBuilder builder() {
        return new OpenAiStreamAgentBuilder();
    }

    /**
     * 指定调用方自行管理生命周期的 WebClient。
     * <p>
     * 此方式不能与 {@link #runtimeConfig(HttpRuntimeConfig)}
     * 或 {@link #registry(WebClientRegistry)} 组合使用。
     *
     * @param webClient 已构建的 WebClient
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder webClient(WebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient must not be null");
        return this;
    }

    /**
     * 指定 HTTP 运行时配置。
     * <p>
     * 未设置 {@link #registry(WebClientRegistry)} 时，构建阶段使用框架全局共享的
     * WebClientRegistry 复用流式 WebClient。
     *
     * @param runtimeConfig HTTP 运行时配置
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder runtimeConfig(HttpRuntimeConfig runtimeConfig) {
        this.runtimeConfig = Objects.requireNonNull(
                runtimeConfig,
                "runtimeConfig must not be null"
        );
        return this;
    }

    /**
     * 指定调用方管理的 WebClientRegistry。
     * <p>
     * 必须同时通过 {@link #runtimeConfig(HttpRuntimeConfig)} 指定运行时配置。
     *
     * @param registry WebClientRegistry 实例
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder registry(WebClientRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        return this;
    }

    /**
     * 指定自定义工具执行器。
     * 未设置时使用默认的 ReflectionToolExecutor。
     *
     * @param toolExecutor 工具执行器实例
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder toolExecutor(ToolExecutor toolExecutor) {
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
        return this;
    }

    /**
     * 追加一个流式步骤 Hook。
     *
     * @param hook 流式步骤 Hook
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder addHook(StreamStepHook hook) {
        this.hooks.add(Objects.requireNonNull(hook, "hook must not be null"));
        return this;
    }

    /**
     * 设置全部流式步骤 Hook，替换此前已添加的 Hook。
     *
     * @param hooks 流式步骤 Hook 数组；传入 null 时清空 Hook
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder hooks(StreamStepHook... hooks) {
        this.hooks.clear();
        if (hooks != null) {
            for (StreamStepHook hook : hooks) {
                addHook(hook);
            }
        }
        return this;
    }

    /**
     * 设置全部流式步骤 Hook，替换此前已添加的 Hook。
     *
     * @param hooks 流式步骤 Hook 列表；传入 null 时清空 Hook
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder hooks(List<? extends StreamStepHook> hooks) {
        this.hooks.clear();
        if (hooks != null) {
            hooks.forEach(this::addHook);
        }
        return this;
    }

    /**
     * 设置单次 Agent 编排的最大步骤数。
     *
     * @param maxStepCount 最大步骤数，必须大于 0
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder maxStepCount(int maxStepCount) {
        if (maxStepCount <= 0) {
            throw new IllegalArgumentException("maxStepCount must be positive");
        }
        this.maxStepCount = maxStepCount;
        return this;
    }

    /**
     * 设置最大模型调用轮次。
     *
     * @param maxIterations 最大调用轮次，必须大于 0
     * @return 当前构造器
     */
    public OpenAiStreamAgentBuilder maxIterations(int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }
        this.maxIterations = maxIterations;
        return this;
    }

    /**
     * 根据已配置的客户端来源和执行参数创建流式 Agent。
     * <p>
     * 客户端来源二选一：
     * <ul>
     *     <li>{@link #webClient(WebClient)}</li>
     *     <li>{@link #runtimeConfig(HttpRuntimeConfig)}，可选结合
     *         {@link #registry(WebClientRegistry)}</li>
     * </ul>
     *
     * @return 已构建的流式 Agent
     * @throws IllegalStateException 未配置客户端来源，或配置了互斥的客户端来源
     */
    public OpenAiStreamAgent build() {
        List<StreamStepHook> configuredHooks = List.copyOf(hooks);
        ToolExecutor executor = toolExecutor != null ? toolExecutor : new ReflectionToolExecutor();

        if (webClient != null) {
            if (runtimeConfig != null || registry != null) {
                throw new IllegalStateException(
                        "webClient cannot be combined with runtimeConfig or registry"
                );
            }
            return OpenAiStreamAgents.create(
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
            return OpenAiStreamAgents.create(
                    registry,
                    runtimeConfig,
                    configuredHooks,
                    maxStepCount,
                    maxIterations,
                    executor
            );
        }

        return OpenAiStreamAgents.create(
                runtimeConfig,
                configuredHooks,
                maxStepCount,
                maxIterations,
                executor
        );
    }
}