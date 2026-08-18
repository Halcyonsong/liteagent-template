package io.github.halcyonsong.liteagent.core.tool.norm;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 工具注册器规范。
 */
public interface ToolRegistrar {

    void register(Object toolObject, ToolRegistry registry);

    void register(Object toolObject, Method method, ToolRegistry registry);

    default void registerAll(List<?> toolObjects, ToolRegistry registry) {
        if (toolObjects == null || toolObjects.isEmpty()) {
            return;
        }
        for (Object toolObject : toolObjects) {
            register(toolObject, registry);
        }
    }

}