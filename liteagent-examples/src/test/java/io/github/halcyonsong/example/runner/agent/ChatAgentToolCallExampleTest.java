package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.core.tool.impl.ToolRegistries;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiConfig.class)
class ChatAgentToolCallExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_agent_should_execute_multi_round_tool_calls() {
        assumeConfigReady();

        ToolRegistry registry = ToolRegistries.inMemory(new WeatherTools());

        OpenAiChatAgent agent = OpenAiChatAgents.create(buildRuntimeConfig());

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一位助手，请使用提供的工具回答用户的问题。"))
                .addMessage(Messages.user("北京和上海今天天气怎么样？"))
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

        OpenAiChatCompletionResponse response = agent.execute(request);
        Printers.printChatResponse(response);

        response.getChoices().forEach(choice ->
                choice.getChatResponse().getMessages().forEach(message -> {
                    if (message instanceof AssistantResponseMessage arm && !arm.getToolCalls().isEmpty()) {
                        System.out.println("模型调用了 " + arm.getToolCalls().size() + " 个工具");
                        arm.getToolCalls().forEach(tc ->
                                System.out.println("  -> " + tc.getFunction().getName()
                                        + "(" + tc.getFunction().getArguments() + ")"));
                    }
                })
        );
    }

    @ToolComponent
    public static class WeatherTools {

        @ToolMethod(name = "get_weather", description = "获取指定城市的当前天气信息")
        public String getWeather(
                @ToolParam(description = "城市名称，例如：北京") String city
        ) {
            return city + "：晴，气温 28°C，湿度 45%";
        }
    }
}
