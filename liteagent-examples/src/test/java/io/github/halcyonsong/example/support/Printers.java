package io.github.halcyonsong.example.support;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiToolCall;

public final class Printers {

    private Printers() {
    }

    public static void printProviderResponse(OpenAiChatCompletionResponse response) {
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
                        for (OpenAiToolCall toolCall : openAiMessage.getToolCalls()) {
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

    public static void printProviderStreamResponse(OpenAiStreamCompletionResponse response) {
        System.out.println("========== OpenAiStreamCompletionResponse ==========");
        System.out.println("response id = " + response.getBaseResponse().getId());
        System.out.println("object = " + response.getBaseResponse().getObject());
        System.out.println("model = " + response.getBaseResponse().getModel());

        for (StreamChoice choice : response.getChoices()) {
            System.out.println("choice index = " + choice.getIndex());
            System.out.println("finish reason = " + choice.getFinishReason());

            StreamDelta delta = choice.getDelta();
            if (delta != null) {
                System.out.println("delta role = " + delta.getRole());
                System.out.println("delta content = " + delta.getContent());
                System.out.println("delta reasoning content = " + delta.getReasoningContent());
            }
        }

        if (response.getUsage() != null) {
            System.out.println("prompt tokens = " + response.getUsage().getPromptTokens());
            System.out.println("completion tokens = " + response.getUsage().getCompletionTokens());
            System.out.println("total tokens = " + response.getUsage().getTotalTokens());
        }
    }

    public static void printStreamDeltaContentAndReasoning(OpenAiStreamCompletionResponse response) {
        response.getChoices().forEach(choice -> {
            if (choice.getDelta() == null) {
                return;
            }

            String content = choice.getDelta().getContent();
            String reasoning = choice.getDelta().getReasoningContent();

            if (reasoning != null && !reasoning.isBlank()) {
                System.out.print(reasoning);
            }

            if (content != null && !content.isBlank()) {
                System.out.print(content);
            }
        });
    }
}
