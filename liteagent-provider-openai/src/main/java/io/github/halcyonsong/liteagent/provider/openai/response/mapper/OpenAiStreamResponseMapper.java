package io.github.halcyonsong.liteagent.provider.openai.response.mapper;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenAI-compatible 流式响应映射器。
 * <p>
 * 负责将原始协议流式 chunk 响应转换为 provider 层流式响应包装对象。
 */
public class OpenAiStreamResponseMapper extends OpenAiResponseMappingSupport {

    public OpenAiStreamCompletionResponse fromRaw(OpenAiChatCompletionRawResponse rawResponse) {
        List<StreamChoice> choices = mapChoices(rawResponse);
        OpenAiBaseResponse baseResponse = mapBaseResponse(rawResponse);
        OpenAiUsage usage = mapUsage(rawResponse.getUsage());

        return new OpenAiStreamCompletionResponse(baseResponse, choices, usage);
    }

    private List<StreamChoice> mapChoices(OpenAiChatCompletionRawResponse rawResponse) {
        if (rawResponse.getChoices() == null || rawResponse.getChoices().isEmpty()) {
            return Collections.emptyList();
        }

        List<StreamChoice> result = new ArrayList<>();
        for (OpenAiChatCompletionRawResponse.RawChoice rawChoice : rawResponse.getChoices()) {
            StreamDelta delta = mapDelta(rawChoice.getDelta());
            FinishReason finishReason = mapStreamFinishReason(rawChoice.getFinishReason());

            result.add(new StreamChoice(
                    rawChoice.getIndex(),
                    delta,
                    finishReason
            ));
        }
        return result;
    }

    private StreamDelta mapDelta(OpenAiChatCompletionRawResponse.RawMessage rawDelta) {
        if (rawDelta == null) {
            return new StreamDelta(
                    null,
                    null,
                    null,
                    Collections.emptyList()
            );
        }

        return new StreamDelta(
                rawDelta.getRole(),
                rawDelta.getContent(),
                rawDelta.getReasoningContent(),
                mapToolCalls(rawDelta.getToolCalls())
        );
    }

    /**
     * 流式中间 chunk 的 finish_reason 通常为 null。
     * <p>
     * 这里必须保留 null 语义，不能映射为 UNKNOWN，
     * 否则上层可能把普通中间 chunk 误判为结束 chunk。
     */
    private FinishReason mapStreamFinishReason(String finishReason) {
        if (finishReason == null) {
            return null;
        }
        return mapFinishReason(finishReason);
    }

}