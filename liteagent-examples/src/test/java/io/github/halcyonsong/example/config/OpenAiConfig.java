package io.github.halcyonsong.example.config;

import io.github.halcyonsong.example.tool.WeatherTools;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.memory.hook.MemoryHooks;
import io.github.halcyonsong.liteagent.memory.window.MemoryWindows;
import io.github.halcyonsong.liteagent.memory.window.norm.MemoryWindowStore;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring Bean 配置，演示如何通过依赖注入组装 LiteAgent 各组件。
 *
 * <ul>
 *   <li>{@link #chatRuntimeConfig} / {@link #streamRuntimeConfig} — HTTP 运行时配置</li>
 *   <li>{@link #baseRequest} — 基础请求（baseUrl / apiKey / model）</li>
 *   <li>{@link #weatherTools} / {@link #toolRegistry} — 工具注册</li>
 *   <li>{@link #memoryWindowStore} — 记忆窗口存储</li>
 *   <li>{@link #chatAgent} / {@link #streamAgent} — 基础 Agent（无 hook）</li>
 *   <li>{@link #chatAgentWithMemory} / {@link #streamAgentWithMemory} — 带记忆窗口的 Agent</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    // ─── Runtime Config ──────────────────────────────────────────

    @Bean("chatRuntimeConfig")
    public HttpRuntimeConfig chatRuntimeConfig(OpenAiProperties props) {
        OpenAiProperties.Runtime r = props.getRuntime();
        return HttpRuntimeConfig.builder()
                .maxInMemorySize(r.getMaxInMemorySize())
                .connectTimeoutMillis(r.getConnectTimeoutMillis())
                .responseTimeoutMillis(r.getResponseTimeoutMillis())
                .build();
    }

    @Bean("streamRuntimeConfig")
    public HttpRuntimeConfig streamRuntimeConfig(OpenAiProperties props) {
        OpenAiProperties.Runtime r = props.getRuntime();
        return HttpRuntimeConfig.builder()
                .maxInMemorySize(r.getMaxInMemorySize())
                .connectTimeoutMillis(r.getConnectTimeoutMillis())
                .streamResponseTimeoutMillis(r.getStreamResponseTimeoutMillis())
                .build();
    }

    // ─── Base Request ────────────────────────────────────────────

    @Bean
    public OpenAiBaseRequest baseRequest(OpenAiProperties props) {
        return OpenAiBaseRequest.builder()
                .baseUrl(props.getBaseUrl())
                .apiKey(props.getApiKey())
                .model(props.getModel())
                .build();
    }

    // ─── Tools ───────────────────────────────────────────────────

    @Bean
    public WeatherTools weatherTools() {
        return new WeatherTools();
    }

    @Bean
    public ToolRegistry toolRegistry(WeatherTools weatherTools) {
        return ToolRegistries.inMemory(weatherTools);
    }

    // ─── Memory ──────────────────────────────────────────────────

    @Bean
    public MemoryWindowStore memoryWindowStore() {
        return MemoryWindows.inMemory();
    }

    // ─── Agents（基础，无 hook）──────────────────────────────────

    @Bean
    public OpenAiChatAgent chatAgent(@Qualifier("chatRuntimeConfig") HttpRuntimeConfig chatRuntimeConfig) {
        return OpenAiChatAgents.create(chatRuntimeConfig);
    }

    @Bean
    public OpenAiStreamAgent streamAgent(@Qualifier("streamRuntimeConfig") HttpRuntimeConfig streamRuntimeConfig) {
        return OpenAiStreamAgents.create(streamRuntimeConfig);
    }

    // ─── Agents（带记忆窗口 hook）────────────────────────────────

    @Bean("chatAgentWithMemory")
    public OpenAiChatAgent chatAgentWithMemory(
            @Qualifier("chatRuntimeConfig") HttpRuntimeConfig chatRuntimeConfig,
            MemoryWindowStore memoryWindowStore,
            OpenAiProperties props
    ) {
        StepHook memoryHook = MemoryHooks.chat(memoryWindowStore, props.getMemory().getChatMaxSize());
        return OpenAiChatAgents.create(
                chatRuntimeConfig,
                List.of(memoryHook),
                1000, 10
        );
    }

    @Bean("streamAgentWithMemory")
    public OpenAiStreamAgent streamAgentWithMemory(
            @Qualifier("streamRuntimeConfig") HttpRuntimeConfig streamRuntimeConfig,
            MemoryWindowStore memoryWindowStore,
            OpenAiProperties props
    ) {
        StreamStepHook memoryHook = MemoryHooks.stream(memoryWindowStore, props.getMemory().getStreamMaxSize());
        return OpenAiStreamAgents.create(
                streamRuntimeConfig,
                List.of(memoryHook),
                1000, 10
        );
    }
}
