package io.github.halcyonsong.example.runner.openai;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.core.tool.impl.InMemoryToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistrar;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.advisor.OpenAiToolChoiceAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = OpenAiConfig.class)
class ProviderToolCallExampleTest extends OpenAiExampleSupport {

    @Autowired
    private OpenAiChatClient client;

    @Test
    void chat_should_return_standard_tool_calls_when_tool_choice_is_forced() {
        assumeConfigReady();

        ToolRegistry registry = new InMemoryToolRegistry();
        ToolRegistrar registrar = new ReflectionToolRegistrar();
        registrar.register(new WeatherTools(), registry);

        assertTrue(registry.contains("get_weather"));

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

        OpenAiChatCompletionResponse response = client.chatCompletion(request);

        System.out.println("==== full response ====");
        System.out.println(response.toJson());

        response.getChoices().forEach(choice -> choice.getChatResponse().getMessages().forEach(message -> {
            System.out.println("role = " + message.getRole());
            System.out.println("content = " + message.getContent());

            if (message instanceof OpenAiAssistantMessage assistantMessage) {
                System.out.println("reasoning = " + assistantMessage.getReasoningContent());

                if (assistantMessage.getToolCalls() != null && !assistantMessage.getToolCalls().isEmpty()) {
                    assistantMessage.getToolCalls().forEach(toolCall -> {
                        System.out.println("tool call id = " + toolCall.getId());
                        System.out.println("tool call type = " + toolCall.getType());
                        if (toolCall.getFunction() != null) {
                            System.out.println("tool name = " + toolCall.getFunction().getName());
                            System.out.println("tool arguments = " + toolCall.getFunction().getArguments());
                        }
                    });
                } else {
                    System.out.println("tool calls = []");
                }
            }

            System.out.println("-----");
        }));
    }

    @ToolComponent
    public static class WeatherTools {

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
            return city + "，" + unit + "，32度";
        }
    }
}