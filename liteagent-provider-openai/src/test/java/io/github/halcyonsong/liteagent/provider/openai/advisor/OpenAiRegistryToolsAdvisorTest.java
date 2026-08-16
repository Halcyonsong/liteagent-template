package io.github.halcyonsong.liteagent.provider.openai.advisor;

import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiRegistryToolsAdvisorTest {

    @Test
    void should_enhance_raw_request_with_tools() {
        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        OpenAiRegistryToolsAdvisor advisor = new OpenAiRegistryToolsAdvisor(registry);
        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.openai.com")
                        .apiKey("test-key")
                        .model("gpt-test")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
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
        assertEquals("获取指定城市的天气", function.get("description"));
    }

    @Test
    void should_not_enhance_when_registry_empty() {
        ToolRegistry registry = ToolRegistries.inMemory();

        OpenAiRegistryToolsAdvisor advisor = new OpenAiRegistryToolsAdvisor(registry);
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();

        advisor.enhance(null, rawRequest);

        assertNull(rawRequest.getTools());
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
