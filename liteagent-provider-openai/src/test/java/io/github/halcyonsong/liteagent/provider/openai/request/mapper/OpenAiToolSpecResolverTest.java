package io.github.halcyonsong.liteagent.provider.openai.request.mapper;

import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiToolSpecResolverTest {

    private final OpenAiToolSpecResolver resolver = new OpenAiToolSpecResolver();

    @Test
    void should_resolve_registry_to_openai_tool_specs() {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        List<OpenAiToolSpec> specs = resolver.resolve(registry);

        assertEquals(1, specs.size());
        assertEquals("function", specs.get(0).getType());
        assertNotNull(specs.get(0).getFunction());
        assertEquals("get_weather", specs.get(0).getFunction().getName());
        assertEquals("获取指定城市的天气", specs.get(0).getFunction().getDescription());
    }

    @Test
    void should_resolve_single_tool_definition() {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        OpenAiToolSpec spec = resolver.resolve(registry.get("get_weather"));

        assertEquals("function", spec.getType());
        assertNotNull(spec.getFunction());
        assertEquals("get_weather", spec.getFunction().getName());
    }

    @ToolComponent
    public static class WeatherTools {

        @ToolMethod(name = "get_weather", description = "获取指定城市的天气")
        public String getWeather(
                @ToolParam(description = "城市名") String city
        ) {
            return city + " 晴 32度";
        }
    }
}
