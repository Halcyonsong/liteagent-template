package io.github.halcyonsong.liteagent.provider.openai.request.mapper;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible 请求映射器。
 * <p>
 * 该映射器负责将 provider 层请求包装对象转换为可直接发送的 raw request。
 */
public class OpenAiChatRequestMapper {

    /**
     * 将 OpenAI provider 请求包装对象映射为原始请求体。
     *
     * @param request provider 请求包装对象
     * @return 可直接序列化并发送的原始请求体
     */
    public OpenAiChatCompletionRawRequest toRawRequest(OpenAiChatCompletionRequest request) {
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();
        rawRequest.setModel(request.getBaseRequest().getModel());
        rawRequest.setMessages(mapMessages(request.getChatRequest().getMessages()));

        if (request.getChatOptions() != null) {
            rawRequest.setTemperature(request.getChatOptions().getTemperature());
            rawRequest.setMaxTokens(request.getChatOptions().getMaxTokens());
        }

        if(request.getCompletionOptions() != null) {
            rawRequest.setTopP(request.getCompletionOptions().getTopP());
            rawRequest.setN(request.getCompletionOptions().getN());
            rawRequest.setPresencePenalty(request.getCompletionOptions().getPresencePenalty());
            rawRequest.setFrequencyPenalty(request.getCompletionOptions().getFrequencyPenalty());
            rawRequest.setResponseFormat(request.getCompletionOptions().getResponseFormat());

            if (request.getCompletionOptions().getStop() != null) {
                rawRequest.setStop(request.getCompletionOptions().getStop().toRawValue());
            }
        }

        return rawRequest;
    }

    private List<Map<String, Object>> mapMessages(List<Message> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("role", message.getRole().name().toLowerCase());
            item.put("content", message.getContent());
            result.add(item);
        }
        return result;
    }
}