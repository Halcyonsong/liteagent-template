package io.github.halcyonsong.liteagent.provider.openai.request.config.tool;

import io.github.halcyonsong.liteagent.core.tool.impl.InMemoryToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiToolSpecResolverTest {

    private final OpenAiToolSpecResolver resolver = new OpenAiToolSpecResolver();

    @Test
    void should_resolve_registry_to_openai_tool_specs() {
        ToolRegistry registry = new InMemoryToolRegistry();
        registry.register(
                SimpleToolDefinition.builder()
                        .name("get_weather")
                        .description("获取指定城市的当前天气信息")
                        .parameters(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "city", Map.of("type", "string", "description", "城市名")
                                ),
                                "required", List.of("city")
                        ))
                        .build()
        );

        List<OpenAiToolSpec> specs = resolver.resolve(registry);

        assertEquals(1, specs.size());
        assertEquals("function", specs.get(0).getType());
        assertNotNull(specs.get(0).getFunction());
        assertEquals("get_weather", specs.get(0).getFunction().getName());
        assertEquals("获取指定城市的当前天气信息", specs.get(0).getFunction().getDescription());
    }

    @Test
    void should_resolve_single_tool_definition() {
        ToolRegistry registry = new InMemoryToolRegistry();
        registry.register(
                SimpleToolDefinition.builder()
                        .name("get_weather")
                        .description("获取指定城市的当前天气信息")
                        .parameters(Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "city", Map.of("type", "string", "description", "城市名")
                                ),
                                "required", List.of("city")
                        ))
                        .build()
        );

        OpenAiToolSpec spec = resolver.resolve(registry.get("get_weather"));

        assertEquals("function", spec.getType());
        assertNotNull(spec.getFunction());
        assertEquals("get_weather", spec.getFunction().getName());
    }
}