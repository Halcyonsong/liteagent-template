package io.github.halcyonsong.liteagent.provider.openai.agent.stream.state;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI 单轮流式响应聚合器。
 * <p>
 * 当前先提供最小骨架：
 * 1. 保留最后一个非空 baseResponse
 * 2. 保留最后一个非空 usage
 * 3. 收集所有 choice
 * 4. 记录最后一个非空 finishReason
 * <p>
 * 后续可继续增强 content、reasoning、tool_calls 的细粒度聚合。
 */
public class OpenAiStreamRoundAccumulator {

    private OpenAiBaseResponse baseResponse;
    private OpenAiUsage usage;
    private final List<StreamChoice> choices = new ArrayList<>();
    @Getter
    private FinishReason finishReason;

    public void accumulate(OpenAiStreamCompletionResponse chunk) {
        if (chunk == null) {
            return;
        }

        if (chunk.getBaseResponse() != null) {
            this.baseResponse = chunk.getBaseResponse();
        }
        if (chunk.getUsage() != null) {
            this.usage = chunk.getUsage();
        }
        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            this.choices.addAll(chunk.getChoices());
            chunk.getChoices().forEach(choice -> {
                if (choice.getFinishReason() != null) {
                    this.finishReason = choice.getFinishReason();
                }
            });
        }
    }

    public boolean hasChoices() {
        return !choices.isEmpty();
    }

    public OpenAiStreamCompletionResponse toFinalResponse() {
        return new OpenAiStreamCompletionResponse(baseResponse, List.copyOf(choices), usage);
    }
}