package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.advisor.OpenAiRegistryToolsAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Chat Agent + 工具调用示例：注入 {@code toolRegistry}，通过 {@code OpenAiRegistryToolsAdvisor} 在请求级注入工具。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class ChatAgentToolCallExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_agent_should_execute_multi_round_tool_calls() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("你是一位助手，请使用提供的工具回答用户的问题。"))
                .addMessage(Messages.user("北京和上海今天天气怎么样？"))
                .build();

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .completionOptions(OpenAiCompletionOptions.builder()
                        .temperature(0.0)
                        .maxTokens(512)
                        .build())
                .requestAdvisor(new OpenAiRegistryToolsAdvisor(toolRegistry))
                .build();

        OpenAiChatCompletionResponse response = chatAgent.execute(request);
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
}
