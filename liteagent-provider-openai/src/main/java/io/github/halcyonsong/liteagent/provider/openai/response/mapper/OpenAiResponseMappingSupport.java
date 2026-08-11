package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiFunctionCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.tool.OpenAiToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenAiResponseMappingSupport {

    protected OpenAiBaseResponse mapBaseResponse(OpenAiChatCompletionRawResponse rawResponse) {
        return new OpenAiBaseResponse(
                rawResponse.getId(),
                rawResponse.getObject(),
                rawResponse.getCreated(),
                rawResponse.getModel()
        );
    }

    protected OpenAiUsage mapUsage(OpenAiChatCompletionRawResponse.RawUsage rawUsage) {
        if (rawUsage == null) {
            return null;
        }
        return new OpenAiUsage(
                rawUsage.getPromptTokens(),
                rawUsage.getCompletionTokens(),
                rawUsage.getTotalTokens(),
                rawUsage.getCompletionTokensDetails(),
                rawUsage.getPromptTokensDetails(),
                rawUsage.getPromptCacheHitTokens(),
                rawUsage.getPromptCacheMissTokens()
        );
    }

    protected FinishReason mapFinishReason(String finishReason) {
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

    protected List<OpenAiToolCall> mapToolCalls(List<OpenAiChatCompletionRawResponse.RawToolCall> rawToolCalls) {
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

    protected OpenAiFunctionCall mapFunction(OpenAiChatCompletionRawResponse.RawFunction rawFunction) {
        if (rawFunction == null) {
            return null;
        }
        return new OpenAiFunctionCall(
                rawFunction.getName(),
                rawFunction.getArguments()
        );
    }
}