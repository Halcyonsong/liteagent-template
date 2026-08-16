package io.github.halcyonsong.example.runner.openai;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiToolChoiceAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ProviderToolCallExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_should_return_tool_calls_when_tool_choice_is_forced() {
        assumeConfigReady();

        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());
        System.out.println("Registered tools: get_weather");

        OpenAiChatAgent agent = OpenAiChatAgents.create(buildRuntimeConfig());

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("北京今天天气怎么样？请优先使用工具。"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.0)
                        .maxTokens(256)
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(registry))
                .requestAdvisor(new OpenAiToolChoiceAdvisor(OpenAiToolChoice.function("get_weather")))
                .build();

        OpenAiChatCompletionResponse response = agent.execute(request);
        Printers.printChatResponse(response);
    }

    @ToolComponent
    public static class WeatherTools {

        @ToolMethod(
                name = "get_weather",
                description = "获取指定城市的当前天气信息，包括温度、天气状况等"
        )
        public String getWeather(
                @ToolParam(description = "需要查询天气的城市名称，例如：北京") String city,
                @ToolParam(description = "温度单位，默认摄氏度", required = false) String unit
        ) {
            return city + "，" + (unit != null ? unit : "摄氏度") + "，32度";
        }
    }
}
