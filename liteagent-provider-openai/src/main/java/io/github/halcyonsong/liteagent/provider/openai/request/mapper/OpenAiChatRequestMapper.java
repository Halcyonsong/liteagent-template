package io.github.halcyonsong.liteagent.provider.openai.request.mapper;

import io.github.halcyonsong.liteagent.core.message.norm.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiCompletionOptions;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat 请求映射器。将编排层 ChatRequest 转换为 raw request，
 * 保留 assistant tool_calls 和 tool tool_call_id 等协议字段。
 */
@Slf4j
public class OpenAiChatRequestMapper {

    public OpenAiChatCompletionRawRequest toRawRequest(OpenAiChatCompletionRequest request) {
        OpenAiChatCompletionRawRequest rawRequest = new OpenAiChatCompletionRawRequest();
        rawRequest.setModel(request.getBaseRequest().getModel());
        rawRequest.setMessages(mapMessages(request.getChatRequest().getMessages()));

        OpenAiCompletionOptions options = request.getCompletionOptions();
        rawRequest.setTemperature(options == null ? null : options.getTemperature());
        rawRequest.setMaxTokens(options == null ? null : options.getMaxTokens());
        rawRequest.setTopP(options == null ? null : options.getTopP());
        rawRequest.setN(options == null ? null : options.getN());
        rawRequest.setPresencePenalty(options == null ? null : options.getPresencePenalty());
        rawRequest.setFrequencyPenalty(options == null ? null : options.getFrequencyPenalty());
        rawRequest.setResponseFormat(options == null ? null : options.getResponseFormat());
        rawRequest.setStop(options == null || options.getStop() == null ? null : options.getStop().toRawValue());
        log.debug("Mapped request. model={}, msgs={}",
                rawRequest.getModel(),
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size()
        );
        return rawRequest;
    }

    /**
     * 映射消息列表到 OpenAI wire format，保留 assistant tool_calls 和 tool tool_call_id。
     */
    private List<Map<String, Object>> mapMessages(List<Message> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("role", message.getRole().name().toLowerCase());
            item.put("content", message.getContent());

            if (message instanceof AssistantResponseMessage assistantMessage
                    && !assistantMessage.getToolCalls().isEmpty()) {
                item.put("tool_calls", mapToolCalls(assistantMessage.getToolCalls()));
            }

            if (message instanceof ToolMessage toolMessage) {
                item.put("tool_call_id", toolMessage.getToolCallId());
            }

            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> mapToolCalls(List<ToolCall> toolCalls) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolCall toolCall : toolCalls) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", toolCall.getId());
            item.put("type", toolCall.getType());
            if (toolCall.getIndex() != null) {
                item.put("index", toolCall.getIndex());
            }

            FunctionCall function = toolCall.getFunction();
            if (function != null) {
                Map<String, Object> functionMap = new LinkedHashMap<>();
                functionMap.put("name", function.getName());
                functionMap.put("arguments", function.getArguments());
                item.put("function", functionMap);
            }

            result.add(item);
        }
        return result;
    }
}