package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.model.response.ChatResult;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.ChatResponse;
import io.github.halcyonsong.liteagent.core.model.response.Usage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiAssistantMessage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiFunctionCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenAI-compatible 响应映射器。
 * <p>
 * 该映射器负责将原始协议响应转换为 provider 层响应包装对象，
 * 并进一步保留向统一 {@link ChatResult} 转换的能力。
 */
public class OpenAiChatResponseMapper {

    /**
     * 将原始响应体映射为 OpenAI provider 响应包装对象。
     *
     * @param rawResponse 原始协议响应体
     * @return provider 层响应包装对象
     */
    public OpenAiChatCompletionResponse fromRaw(OpenAiChatCompletionRawResponse rawResponse) {
        List<ChatChoice> choices = mapChoices(rawResponse);

        OpenAiBaseResponse baseResponse = new OpenAiBaseResponse(
                rawResponse.getId(),
                rawResponse.getObject(),
                rawResponse.getCreated(),
                rawResponse.getModel()
        );

        Usage usage = mapUsage(rawResponse.getUsage());

        return new OpenAiChatCompletionResponse(baseResponse, choices, usage);
    }

    private List<ChatChoice> mapChoices(OpenAiChatCompletionRawResponse rawResponse) {
        if (rawResponse.getChoices() == null || rawResponse.getChoices().isEmpty()) {
            throw new IllegalStateException("OpenAI response choices must not be empty");
        }

        List<ChatChoice> result = new ArrayList<>();
        for (OpenAiChatCompletionRawResponse.RawChoice rawChoice : rawResponse.getChoices()) {
            ChatResponse chatResponse = mapChatResponse(rawChoice);
            FinishReason finishReason = mapFinishReason(rawChoice.getFinishReason());

            result.add(new ChatChoice(
                    rawChoice.getIndex(),
                    chatResponse,
                    finishReason
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
        String reasoningContent = rawMessage.getReasoningContent();

        List<OpenAiToolCall> toolCalls = mapToolCalls(rawMessage.getToolCalls());

        return new OpenAiAssistantMessage(content, reasoningContent, toolCalls);
    }

    private List<OpenAiToolCall> mapToolCalls(List<OpenAiChatCompletionRawResponse.RawToolCall> rawToolCalls) {
        if (rawToolCalls == null || rawToolCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<OpenAiToolCall> result = new ArrayList<>();
        for (OpenAiChatCompletionRawResponse.RawToolCall rawToolCall : rawToolCalls) {
            result.add(new OpenAiToolCall(
                    rawToolCall.getIndex(),
                    rawToolCall.getId(),
                    rawToolCall.getType(),
                    mapFunction(rawToolCall.getFunction())
            ));
        }
        return result;
    }

    private OpenAiFunctionCall mapFunction(OpenAiChatCompletionRawResponse.RawFunction rawFunction) {
        if (rawFunction == null) {
            return null;
        }
        return new OpenAiFunctionCall(
                rawFunction.getName(),
                rawFunction.getArguments()
        );
    }

    private Usage mapUsage(OpenAiChatCompletionRawResponse.RawUsage rawUsage) {
        if (rawUsage == null) {
            return null;
        }
        return new Usage(
                rawUsage.getPromptTokens(),
                rawUsage.getCompletionTokens(),
                rawUsage.getTotalTokens()
        );
    }

    private FinishReason mapFinishReason(String finishReason) {
        if (finishReason == null) {
            return FinishReason.UNKNOWN;
        }
        return switch (finishReason) {
            case "stop" -> FinishReason.STOP;
            case "length" -> FinishReason.LENGTH;
            case "content_filter" -> FinishReason.CONTENT_FILTER;
            case "tool_calls" -> FinishReason.TOOL_CALLS;
            default -> FinishReason.UNKNOWN;
        };
    }
}