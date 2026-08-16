package io.github.halcyonsong.liteagent.provider.openai.advisor;

import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolSpec;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiToolSpecResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 将 ToolRegistry 解析为 OpenAI tools 并写入 rawRequest。
 * <p>
 * 只负责工具定义注入，不负责工具执行。
 */
@Getter
@Slf4j
public class OpenAiRegistryToolsAdvisor implements RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest> {

    private final ToolRegistry registry;
    private final OpenAiToolSpecResolver resolver;

    public OpenAiRegistryToolsAdvisor(ToolRegistry registry) {
        this(registry, new OpenAiToolSpecResolver());
    }

    public OpenAiRegistryToolsAdvisor(ToolRegistry registry,
                                      OpenAiToolSpecResolver resolver) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.resolver = Objects.requireNonNull(resolver, "resolver must not be null");
    }

    @Override
    public void enhance(OpenAiChatCompletionRequest request,
                        OpenAiChatCompletionRawRequest rawRequest) {
        List<OpenAiToolSpec> tools = resolver.resolve(registry);
        log.debug("Resolved OpenAI tools from registry. toolCount={}, toolNames={}",
                tools.size(),
                tools.stream()
                        .map(tool -> tool.getFunction().getName())
                        .toList());
        if (tools.isEmpty()) {
            log.debug("No tools resolved from registry, skipping OpenAI tools injection.");
            return;
        }

        rawRequest.setTools(
                tools.stream()
                        .map(OpenAiToolSpec::toRawValue)
                        .collect(Collectors.toList())
        );
    }
}