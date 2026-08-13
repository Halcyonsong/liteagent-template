package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于内存的工具注册表实现。
 */
public class InMemoryToolRegistry implements ToolRegistry {

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    @Override
    public void register(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool must not be null");
        Objects.requireNonNull(tool.getName(), "tool name must not be null");

        if (tool.getName().isBlank()) {
            throw new IllegalArgumentException("tool name must not be blank");
        }

        tools.put(tool.getName(), tool);
    }

    @Override
    public ToolDefinition get(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return tools.get(name);
    }

    @Override
    public boolean contains(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return tools.containsKey(name);
    }

    @Override
    public List<ToolDefinition> getAll() {
        return new ArrayList<>(tools.values());
    }
}