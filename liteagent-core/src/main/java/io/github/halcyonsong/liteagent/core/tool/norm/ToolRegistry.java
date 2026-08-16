package io.github.halcyonsong.liteagent.core.tool.norm;

import java.util.List;

/**
 * 工具注册表规范。
 * <p>
 * 这里只负责保存工具定义，不负责工具执行。
 */
public interface ToolRegistry {

    /**
     * 注册单个工具。
     */
    void register(ToolDefinition tool);

    /**
     * 批量注册工具。
     */
    default void registerAll(List<? extends ToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return;
        }
        for (ToolDefinition tool : tools) {
            register(tool);
        }
    }

    /**
     * 按名称获取工具定义。
     */
    ToolDefinition get(String name);

    /**
     * 判断工具是否存在。
     */
    boolean contains(String name);

    /**
     * 获取全部已注册工具。
     */
    List<ToolDefinition> getAll();
}