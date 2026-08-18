package io.github.halcyonsong.example.support;

import io.github.halcyonsong.example.config.OpenAiProperties;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 测试基类，通过 Spring DI 注入所有框架组件。
 *
 * 子类可直接使用 {@link #chatAgent}、{@link #streamAgent}、{@link #baseRequest}、{@link #toolRegistry}，
 * 也可通过 {@link #chatAgentWithMemory} / {@link #streamAgentWithMemory} 使用带记忆窗口的 Agent。
 */
public abstract class OpenAiExampleSupport {

    @Autowired
    protected OpenAiProperties properties;

    @Autowired
    protected OpenAiBaseRequest baseRequest;

    @Autowired
    @Qualifier("chatRuntimeConfig")
    protected HttpRuntimeConfig chatRuntimeConfig;

    @Autowired
    @Qualifier("streamRuntimeConfig")
    protected HttpRuntimeConfig streamRuntimeConfig;

    @Autowired
    protected OpenAiChatAgent chatAgent;

    @Autowired
    protected OpenAiStreamAgent streamAgent;

    @Autowired
    protected ToolRegistry toolRegistry;

    @Autowired
    @Qualifier("chatAgentWithMemory")
    protected OpenAiChatAgent chatAgentWithMemory;

    @Autowired
    @Qualifier("streamAgentWithMemory")
    protected OpenAiStreamAgent streamAgentWithMemory;

    protected void assumeConfigReady() {
        Assumptions.assumeTrue(Boolean.TRUE.equals(properties.getEnabled()), "openai example disabled");
        Assumptions.assumeTrue(properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank(), "baseUrl is blank");
        Assumptions.assumeTrue(properties.getApiKey() != null && !properties.getApiKey().isBlank(), "apiKey is blank");
        Assumptions.assumeTrue(properties.getModel() != null && !properties.getModel().isBlank(), "model is blank");
    }
}
