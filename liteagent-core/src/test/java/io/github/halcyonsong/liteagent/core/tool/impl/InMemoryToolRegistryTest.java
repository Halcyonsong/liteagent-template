package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryToolRegistryTest {

    private InMemoryToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryToolRegistry();
    }

    @Test
    void should_register_and_get_tool() {
        ToolDefinition tool = createDefinition("get_weather", "获取天气");
        registry.register(tool);

        assertTrue(registry.contains("get_weather"));
        assertSame(tool, registry.get("get_weather"));
    }

    @Test
    void should_return_null_for_nonexistent_tool() {
        assertNull(registry.get("nonexistent"));
        assertFalse(registry.contains("nonexistent"));
    }

    @Test
    void should_get_all_returns_registered_tools() {
        registry.register(createDefinition("get_weather", "天气"));
        registry.register(createDefinition("get_time", "时间"));

        List<ToolDefinition> all = registry.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void should_get_all_return_empty_when_nothing_registered() {
        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    void should_get_all_be_defensive_copy() {
        registry.register(createDefinition("get_weather", "天气"));

        List<ToolDefinition> all = registry.getAll();
        all.clear();

        assertEquals(1, registry.getAll().size());
    }

    @Test
    void should_overwrite_tool_with_same_name() {
        ToolDefinition first = createDefinition("get_weather", "v1");
        ToolDefinition second = createDefinition("get_weather", "v2");

        registry.register(first);
        registry.register(second);

        assertEquals(1, registry.getAll().size());
        assertEquals("v2", registry.get("get_weather").getDescription());
    }

    @Test
    void should_reject_null_tool() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void should_reject_null_name() {
        ToolDefinition tool = createDefinition(null, "desc");
        assertThrows(NullPointerException.class, () -> registry.register(tool));
    }

    @Test
    void should_reject_blank_name() {
        ToolDefinition tool = createDefinition("  ", "desc");
        assertThrows(IllegalArgumentException.class, () -> registry.register(tool));
    }

    @Test
    void should_reject_null_get_name() {
        assertThrows(NullPointerException.class, () -> registry.get(null));
    }

    @Test
    void should_reject_null_contains_name() {
        assertThrows(NullPointerException.class, () -> registry.contains(null));
    }

    private static ToolDefinition createDefinition(String name, String description) {
        return new ToolDefinition() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public Map<String, Object> getParameters() {
                return Map.of("type", "object");
            }
        };
    }
}
