package io.github.halcyonsong.test;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.request.ChatOptions;
import io.github.halcyonsong.liteagent.core.model.request.ChatRequest;
import io.github.halcyonsong.liteagent.core.model.response.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiChatCompletionResponse;
import io.github.halcyonsong.test.config.OpenAiExampleProperties;
import io.github.halcyonsong.test.config.OpenAiExampleTestConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OpenAiExampleTestConfig.class)
public class OpenAiChatClientExampleTest {

    @Autowired
    private OpenAiChatClient client;

    @Autowired
    private OpenAiExampleProperties properties;

    private void assumeConfigReady() {
        Assumptions.assumeTrue(Boolean.TRUE.equals(properties.getEnabled()), "openai example disabled");
        Assumptions.assumeTrue(properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank(), "baseUrl is blank");
        Assumptions.assumeTrue(properties.getApiKey() != null && !properties.getApiKey().isBlank(), "apiKey is blank");
        Assumptions.assumeTrue(properties.getModel() != null && !properties.getModel().isBlank(), "model is blank");
    }

    private OpenAiBaseRequest createBaseRequest() {
        return OpenAiBaseRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .build();
    }

    private void printChatResult(ChatResult result) {
        System.out.println("========== ChatResult ==========");
        System.out.println("response id = " + result.getBaseResponse().getId());
        System.out.println("object = " + result.getBaseResponse().getObject());
        System.out.println("model = " + result.getBaseResponse().getModel());

        for (ChatChoice choice : result.getChoices()) {
            System.out.println("choice index = " + choice.getIndex());
            System.out.println("finish reason = " + choice.getFinishReason());

            for (Message message : choice.getChatResponse().getMessages()) {
                System.out.println("message role = " + message.getRole());
                System.out.println("message content = " + message.getContent());
            }
        }

        if (result.getUsage() != null) {
            System.out.println("prompt tokens = " + result.getUsage().getPromptTokens());
            System.out.println("completion tokens = " + result.getUsage().getCompletionTokens());
            System.out.println("total tokens = " + result.getUsage().getTotalTokens());
        }
    }

    private void printProviderResponse(OpenAiChatCompletionResponse response) {
        System.out.println("========== OpenAiChatCompletionResponse ==========");
        System.out.println("response id = " + response.getBaseResponse().getId());
        System.out.println("object = " + response.getBaseResponse().getObject());
        System.out.println("model = " + response.getBaseResponse().getModel());

        for (ChatChoice choice : response.getChoices()) {
            System.out.println("choice index = " + choice.getIndex());
            System.out.println("finish reason = " + choice.getFinishReason());

            for (Message message : choice.getChatResponse().getMessages()) {
                System.out.println("message role = " + message.getRole());
                System.out.println("message content = " + message.getContent());

                if (message instanceof OpenAiAssistantMessage openAiMessage) {
                    System.out.println("reasoning content = " + openAiMessage.getReasoningContent());

                    if (openAiMessage.getToolCalls() != null && !openAiMessage.getToolCalls().isEmpty()) {
                        for (var toolCall : openAiMessage.getToolCalls()) {
                            System.out.println("tool call id = " + toolCall.getId());
                            System.out.println("tool call type = " + toolCall.getType());

                            if (toolCall.getFunction() != null) {
                                System.out.println("tool function name = " + toolCall.getFunction().getName());
                                System.out.println("tool function arguments = " + toolCall.getFunction().getArguments());
                            }
                        }
                    }
                }
            }
        }

        if (response.getUsage() != null) {
            System.out.println("prompt tokens = " + response.getUsage().getPromptTokens());
            System.out.println("completion tokens = " + response.getUsage().getCompletionTokens());
            System.out.println("total tokens = " + response.getUsage().getTotalTokens());
        }
    }

    @Test
    void chat_by_invocation_should_return_chat_result() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("你好，请用一句话介绍你自己。"))
                .build();

        ChatOptions chatOptions = ChatOptions.builder()
                .stream(false)
                .temperature(0.7)
                .maxTokens(256)
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .chatOptions(chatOptions)
                .build();

        ChatResult result = client.chat(invocation);
        printChatResult(result);
    }

    @Test
    void chat_by_quick_request_should_return_chat_result() {
        assumeConfigReady();

        OpenAiQuickChatRequest request = OpenAiQuickChatRequest.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .systemMessage("You are a helpful assistant.")
                .userMessage("你好，请简单介绍一下你自己。")
                .build();

        ChatResult result = client.chat(request);
        printChatResult(result);
    }

    @Test
    void chat_by_invocation_should_return_provider_response() {
        assumeConfigReady();

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.system("You are a helpful assistant."))
                .addMessage(Messages.user("请简单回复：测试 provider response。"))
                .build();

        ChatInvocation invocation = ChatInvocation.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .chatOptions(null)
                .build();

        OpenAiChatCompletionResponse response = client.chatCompletion(invocation);
        printProviderResponse(response);
    }
}