package io.github.halcyonsong.liteagent.provider.openai.response.config.chat;

import io.github.halcyonsong.liteagent.core.model.response.Result;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * OpenAI-compatible chat completions 响应包装对象。
 * <p>
 * 该对象保留 provider 层语义，封装基础响应信息、候选结果列表以及 token 用量，
 * 同时可进一步转换为框架统一的 {@link Result}。
 */
@Getter
public class OpenAiChatCompletionResponse implements Result {

    private final OpenAiBaseResponse baseResponse;
    private final List<ChatChoice> choices;
    private final OpenAiUsage usage;

    public OpenAiChatCompletionResponse(OpenAiBaseResponse baseResponse,
                                        List<ChatChoice> choices,
                                        OpenAiUsage usage) {
        this.baseResponse = Objects.requireNonNull(baseResponse, "baseResponse must not be null");
        Objects.requireNonNull(choices, "choices must not be null");
        this.choices = List.copyOf(choices);
        this.usage = usage;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

    @Override
    public String toString() {
        return "OpenAiChatCompletionResponse{" +
                "responseId='" + baseResponse.getId() + '\'' +
                ", model='" + baseResponse.getModel() + '\'' +
                ", choiceCount=" + choices.size() +
                ", usage=" + usage +
                '}';
    }
}