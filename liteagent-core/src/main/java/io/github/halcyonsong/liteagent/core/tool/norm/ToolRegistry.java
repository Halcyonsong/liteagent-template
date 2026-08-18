package io.github.halcyonsong.liteagent.core.tool.norm;

import java.util.List;

/**
 * 工具注册表规范，只负责保存工具定义，不负责执行。
 */
public interface ToolRegistry {

    void register(ToolDefinition tool);

    default void registerAll(List<? extends ToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return;
        }
        for (ToolDefinition tool : tools) {
            register(tool);
        }
    }

    ToolDefinition get(String name);

    boolean contains(String name);

    List<ToolDefinition> getAll();
}