package io.github.halcyonsong.liteagent.provider.openai.response.config.stream;

import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI-compatible 流式 completion 响应包装对象。
 * <p>
 * 该对象表示一次流式返回中的单个 chunk，
 * 顶层保留 provider 响应语义，内部复用 core 流式结构。
 */
@Getter
public class OpenAiStreamCompletionResponse {

    private final OpenAiBaseResponse baseResponse;
    private final List<StreamChoice> choices;
    private final OpenAiUsage usage;

    public OpenAiStreamCompletionResponse(
            OpenAiBaseResponse baseResponse,
            List<StreamChoice> choices,
            OpenAiUsage usage
    ) {
        this.baseResponse = Objects.requireNonNull(baseResponse, "baseResponse must not be null");
        Objects.requireNonNull(choices, "choices must not be null");
        this.choices = List.copyOf(choices);
        this.usage = usage;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    public String toCompactJson() {
        return JsonSupport.toCompactJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiStreamCompletionResponse{" +
                "responseId='" + baseResponse.getId() + '\'' +
                ", model='" + baseResponse.getModel() + '\'' +
                ", choiceCount=" + choices.size() +
                ", usage=" + usage +
                '}';
    }

}