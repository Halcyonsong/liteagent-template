package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolRegistriesTest {

    @Test
    void should_create_empty_in_memory_registry() {
        ToolRegistry registry = ToolRegistries.inMemory();

        assertNotNull(registry);
        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    void should_register_tools_by_varargs() {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        assertTrue(registry.contains("get_weather"));
        assertEquals(1, registry.getAll().size());
    }

    @ToolComponent
    static class WeatherTools {

        @ToolMethod(
                name = "get_weather",
                description = "获取指定城市的当前天气信息，包括温度、天气状况等"
        )
        public String getWeather(
                @ToolParam(description = "需要查询天气的城市名称，例如：北京")
                String city
        ) {
            return city;
        }
    }
}