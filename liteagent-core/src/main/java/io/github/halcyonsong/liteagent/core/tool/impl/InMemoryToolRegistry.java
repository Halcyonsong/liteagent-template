package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的工具注册表实现，支持并发注册和查询。
 */
public class InMemoryToolRegistry implements ToolRegistry {

    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    @Override
    public void register(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool must not be null");

        String name = Objects.requireNonNull(tool.getName(), "tool name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("tool name must not be blank");
        }

        tools.put(name, tool);
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