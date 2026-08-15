package io.github.halcyonsong.liteagent.provider.openai.request.advisor;

import io.github.halcyonsong.liteagent.core.model.request.norm.RequestAdvisor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolSpec;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolSpecResolver;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基于工具注册表的 tools 请求增强器。
 */
@Getter
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
        if (tools.isEmpty()) {
            return;
        }

        rawRequest.setTools(
                tools.stream()
                        .map(OpenAiToolSpec::toRawValue)
                        .collect(Collectors.toList())
        );
    }
}