package io.github.halcyonsong.example.runner.openai;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiToolChoiceAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Chat Agent + tool_choice 强制调用示例：通过 {@code OpenAiToolChoiceAdvisor} 强制模型调用指定工具。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class ProviderToolCallExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_should_return_tool_calls_when_tool_choice_is_forced() {
        assumeConfigReady();

        System.out.println("Registered tools: get_weather, get_time");

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("北京今天天气怎么样？请优先使用工具。"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.0)
                        .maxTokens(256)
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(toolRegistry))
                .requestAdvisor(new OpenAiToolChoiceAdvisor(OpenAiToolChoice.function("get_weather")))
                .build();

        OpenAiChatCompletionResponse response = chatAgent.execute(request);
        Printers.printChatResponse(response);
    }
}
