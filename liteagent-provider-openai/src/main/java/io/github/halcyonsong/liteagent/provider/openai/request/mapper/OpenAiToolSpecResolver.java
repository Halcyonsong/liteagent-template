package io.github.halcyonsong.liteagent.provider.openai.request.mapper;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiFunctionSpec;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolSpec;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OpenAI-compatible 工具定义解析器。
 * <p>
 * 负责将 core 层注册的工具定义转换为 OpenAI-compatible tools 请求结构。
 */
public class OpenAiToolSpecResolver {

    public List<OpenAiToolSpec> resolve(ToolRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        return registry.getAll().stream()
                .map(this::resolve)
                .collect(Collectors.toList());
    }

    public OpenAiToolSpec resolve(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool must not be null");

        return OpenAiToolSpec.function(
                OpenAiFunctionSpec.of(
                        tool.getName(),
                        tool.getDescription(),
                        tool.getParameters()
                )
        );
    }
}