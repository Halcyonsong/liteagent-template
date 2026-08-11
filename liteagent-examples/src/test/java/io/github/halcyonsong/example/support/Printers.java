package io.github.halcyonsong.example.support;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChunk;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiToolCall;

public final class Printers {

    private Printers() {
    }

    public static void printChatResult(ChatResult result) {
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

    public static void printStreamChunk(StreamChunk chunk) {
        System.out.println("========== StreamChunk ==========");
        System.out.println("response id = " + chunk.getBaseResponse().getId());
        System.out.println("object = " + chunk.getBaseResponse().getObject());
        System.out.println("model = " + chunk.getBaseResponse().getModel());

        for (StreamChoice choice : chunk.getChoices()) {
            System.out.println("choice index = " + choice.getIndex());
            System.out.println("finish reason = " + choice.getFinishReason());

            StreamDelta delta = choice.getDelta();
            if (delta != null) {
                System.out.println("delta role = " + delta.getRole());
                System.out.println("delta content = " + delta.getContent());
                System.out.println("delta reasoning content = " + delta.getReasoningContent());
            }
        }

        if (chunk.getUsage() != null) {
            System.out.println("prompt tokens = " + chunk.getUsage().getPromptTokens());
            System.out.println("completion tokens = " + chunk.getUsage().getCompletionTokens());
            System.out.println("total tokens = " + chunk.getUsage().getTotalTokens());
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
}