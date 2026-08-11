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
            FinishReason finishReason = mapFinishReason(rawChoice.getFinishReason());

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
            return new StreamDelta(null, null, null);
        }

        return new StreamDelta(
                rawDelta.getRole(),
                rawDelta.getContent(),
                rawDelta.getReasoningContent()
        );
    }
}