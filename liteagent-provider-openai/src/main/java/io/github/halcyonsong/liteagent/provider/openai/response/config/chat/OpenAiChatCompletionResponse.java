package io.github.halcyonsong.liteagent.provider.openai.response.config.chat;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OpenAI-compatible chat completions 响应包装对象。
 * <p>
 * 该对象保留 provider 层语义，封装基础响应信息、候选结果列表以及 token 用量，
 * 同时可进一步转换为框架统一的 {@link ChatResult}。
 */
public class OpenAiChatCompletionResponse {

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

    public OpenAiBaseResponse getBaseResponse() {
        return baseResponse;
    }

    public List<ChatChoice> getChoices() {
        return choices;
    }

    public OpenAiUsage getUsage() {
        return usage;
    }

    public ChatResult toChatResult() {
        List<ChatChoice> normalizedChoices = new ArrayList<>();

        for (ChatChoice choice : choices) {
            List<Message> normalizedMessages = new ArrayList<>();

            for (Message message : choice.getChatResponse().getMessages()) {
                if (message instanceof AssistantMessage assistantMessage) {
                    normalizedMessages.add(new AssistantMessage(assistantMessage.getContent()));
                } else {
                    normalizedMessages.add(message);
                }
            }

            ChatResponse normalizedResponse = new ChatResponse(normalizedMessages);

            normalizedChoices.add(new ChatChoice(
                    choice.getIndex(),
                    normalizedResponse,
                    choice.getFinishReason()
            ));
        }

        return new ChatResult(baseResponse, normalizedChoices, usage);
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