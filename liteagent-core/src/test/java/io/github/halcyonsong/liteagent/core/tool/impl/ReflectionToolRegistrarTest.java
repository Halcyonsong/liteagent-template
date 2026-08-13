package io.github.halcyonsong.liteagent.core.tool.impl;

import io.github.halcyonsong.liteagent.core.tool.norm.ToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionToolRegistrarTest {

    private final ToolRegistrar registrar = new ReflectionToolRegistrar();

    @Test
    void should_register_annotated_tool_method() {
        ToolRegistry registry = new InMemoryToolRegistry();

        registrar.register(new WeatherTools(), registry);

        assertTrue(registry.contains("get_weather"));

        ToolDefinition definition = registry.get("get_weather");
        assertNotNull(definition);
        assertEquals("get_weather", definition.getName());
        assertEquals("获取指定城市的当前天气信息，包括温度、天气状况等", definition.getDescription());

        Map<String, Object> parameters = definition.getParameters();
        assertEquals("object", parameters.get("type"));
        assertTrue(parameters.containsKey("properties"));
        assertTrue(parameters.containsKey("required"));
    }

    @Test
    void should_ignore_class_without_tool_component() {
        ToolRegistry registry = new InMemoryToolRegistry();

        registrar.register(new NotToolComponentTools(), registry);

        assertFalse(registry.contains("get_weather"));
        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    void should_mark_required_and_optional_parameters_correctly() {
        ToolRegistry registry = new InMemoryToolRegistry();

        registrar.register(new WeatherTools(), registry);

        ToolDefinition definition = registry.get("get_weather");
        assertNotNull(definition);

        Map<String, Object> parameters = definition.getParameters();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) parameters.get("properties");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) parameters.get("required");

        assertNotNull(properties);
        assertNotNull(required);

        assertTrue(properties.containsKey("city"));
        assertTrue(properties.containsKey("unit"));

        assertTrue(required.contains("city"));
        assertFalse(required.contains("unit"));
    }

    @ToolComponent
    static class WeatherTools {

        @ToolMethod(
                name = "get_weather",
                description = "获取指定城市的当前天气信息，包括温度、天气状况等"
        )
        public String getWeather(
                @ToolParam(description = "需要查询天气的城市名称，例如：北京")
                String city,

                @ToolParam(description = "温度单位，默认摄氏度", required = false)
                String unit
        ) {
            return city + "-" + unit;
        }
    }

    static class NotToolComponentTools {

        @ToolMethod(name = "get_weather", description = "should not be registered")
        public String getWeather(String city) {
            return city;
        }
    }
}