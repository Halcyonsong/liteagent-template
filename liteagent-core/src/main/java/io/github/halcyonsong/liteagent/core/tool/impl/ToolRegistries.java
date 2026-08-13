package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;

import java.util.List;

/**
 * 工具注册器工厂。
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
        registrar.registerAll(List.of(toolObjects), registry);
        return registry;
    }
}
