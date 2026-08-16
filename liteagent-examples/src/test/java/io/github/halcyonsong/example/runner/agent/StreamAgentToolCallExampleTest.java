package io.github.halcyonsong.example.runner.agent;

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
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class StreamAgentToolCallExampleTest extends OpenAiExampleSupport {

    @Test
    void stream_agent_should_execute_multi_round_tool_calls() {
        assumeConfigReady();

        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        OpenAiStreamAgent agent = OpenAiStreamAgents.create(buildStreamRuntimeConfig());

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一位助手，请使用提供的工具回答用户的问题。"))
                .addMessage(Messages.user("北京今天天气怎么样？"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.0)
                        .maxTokens(512)
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(registry))
                .build();

        System.out.println("===== Stream Agent Tool Call =====");
        agent.execute(request)
                .doOnNext(Printers::printStreamDeltaAll)
                .blockLast();
        System.out.println("\n===== Stream End =====");
    }

    @ToolComponent
    public static class WeatherTools {

        @ToolMethod(name = "get_weather", description = "获取指定城市的当前天气信息")
        public String getWeather(
                @ToolParam(description = "城市名称") String city
        ) {
            return city + "：多云，气温 25°C，湿度 60%";
        }
    }
}
