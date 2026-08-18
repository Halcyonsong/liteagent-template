package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenAI 响应字段映射辅助类。只做协议字段到统一模型的转换，不负责跨 chunk 聚合。
 */
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

    protected List<ToolCall> mapToolCalls(List<OpenAiChatCompletionRawResponse.RawToolCall> rawToolCalls) {
        if (rawToolCalls == null || rawToolCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolCall> result = new ArrayList<>();
        for (OpenAiChatCompletionRawResponse.RawToolCall rawToolCall : rawToolCalls) {
            result.add(new ToolCall(
                    rawToolCall.getIndex(),
                    rawToolCall.getId(),
                    rawToolCall.getType(),
                    mapFunction(rawToolCall.getFunction())
            ));
        }
        return result;
    }

    protected FunctionCall mapFunction(OpenAiChatCompletionRawResponse.RawFunction rawFunction) {
        if (rawFunction == null) {
            return null;
        }
        return new FunctionCall(
                rawFunction.getName(),
                rawFunction.getArguments()
        );
    }

}