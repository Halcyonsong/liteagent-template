package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenAiChatResponseMapper extends OpenAiResponseMappingSupport {

    public OpenAiChatCompletionResponse fromRaw(OpenAiChatCompletionRawResponse rawResponse) {
        List<ChatChoice> choices = mapChoices(rawResponse);
        OpenAiBaseResponse baseResponse = mapBaseResponse(rawResponse);
        OpenAiUsage usage = mapUsage(rawResponse.getUsage());

        return new OpenAiChatCompletionResponse(baseResponse, choices, usage);
    }

    private List<ChatChoice> mapChoices(OpenAiChatCompletionRawResponse rawResponse) {
        if (rawResponse.getChoices() == null || rawResponse.getChoices().isEmpty()) {
            throw new IllegalStateException("OpenAI response choices must not be empty");
        }

        List<ChatChoice> result = new ArrayList<>();
        for (OpenAiChatCompletionRawResponse.RawChoice rawChoice : rawResponse.getChoices()) {
            ChatResponse chatResponse = mapChatResponse(rawChoice);

            result.add(new ChatChoice(
                    rawChoice.getIndex(),
                    chatResponse,
                    mapFinishReason(rawChoice.getFinishReason())
            ));
        }
        return result;
    }

    private ChatResponse mapChatResponse(OpenAiChatCompletionRawResponse.RawChoice rawChoice) {
        return new ChatResponse(
                Collections.singletonList(mapAssistantMessage(rawChoice.getMessage()))
        );
    }

    private AssistantMessage mapAssistantMessage(OpenAiChatCompletionRawResponse.RawMessage rawMessage) {
        if (rawMessage == null) {
            return new OpenAiAssistantMessage("", null, Collections.emptyList());
        }

        String content = rawMessage.getContent() == null ? "" : rawMessage.getContent();
        List<OpenAiToolCall> toolCalls = mapToolCalls(rawMessage.getToolCalls());

        return new OpenAiAssistantMessage(
                content,
                rawMessage.getReasoningContent(),
                toolCalls
        );
    }
}