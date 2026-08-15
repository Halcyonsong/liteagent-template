package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * 工具注册表工厂。
 */
public final class ToolRegistries {

    private ToolRegistries() {
    }

    public static ToolRegistry inMemory() {
        return new InMemoryToolRegistry();
    }

    public static ToolRegistry inMemory(Object... toolObjects) {
        ToolRegistry registry = new InMemoryToolRegistry();
        ToolRegistrar registrar = new ReflectionToolRegistrar();
        if (toolObjects != null && toolObjects.length > 0) {
            registrar.registerAll(Arrays.asList(toolObjects), registry);
        }
        return registry;
    }

    public static ToolRegistry inMemory(List<?> toolObjects) {
        ToolRegistry registry = new InMemoryToolRegistry();
        ToolRegistrar registrar = new ReflectionToolRegistrar();
        registrar.registerAll(toolObjects, registry);
        return registry;
    }
}