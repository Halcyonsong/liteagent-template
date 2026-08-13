package io.github.halcyonsong.liteagent.provider.openai.request.advisor;

import io.github.halcyonsong.liteagent.core.tool.impl.InMemoryToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.impl.SimpleToolDefinition;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiRegistryToolsAdvisorTest {

    @Test
    void should_enhance_raw_request_with_tools() {
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

        OpenAiRegistryToolsAdvisor advisor = new OpenAiRegistryToolsAdvisor(registry);
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("test-key")
                        .model("gpt-test")
                        .build())
                .chatRequest(io.github.halcyonsong.liteagent.core.model.request.ChatRequest.builder()
                        .addMessage(io.github.halcyonsong.liteagent.core.message.type.constructor.Messages.user("hello"))
                        .build())
                .completionOptions(OpenAiCompletionOptions.builder().build())
                .build();

        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();
        advisor.enhance(request, rawRequest);

        assertNotNull(rawRequest.getTools());
        assertEquals(1, rawRequest.getTools().size());

        Map<String, Object> tool = rawRequest.getTools().get(0);
        assertEquals("function", tool.get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> function = (Map<String, Object>) tool.get("function");
        assertEquals("get_weather", function.get("name"));
        assertEquals("获取指定城市的当前天气信息", function.get("description"));
    }

    @Test
    void should_not_enhance_when_registry_empty() {
        ToolRegistry registry = new InMemoryToolRegistry();

        OpenAiRegistryToolsAdvisor advisor = new OpenAiRegistryToolsAdvisor(registry);
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        advisor.enhance(null, rawRequest);

        assertNull(rawRequest.getTools());
    }
}