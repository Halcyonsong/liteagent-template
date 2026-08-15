package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatChoice;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResponse;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenAI tool call 解析辅助类。
 * <p>
 * 当前负责从 provider response 中提取全部 assistant 响应消息及其 tool calls，
 * 不承担实际工具执行职责。
 */
public final class OpenAiToolCallSupport {

    private OpenAiToolCallSupport() {
    }

    /**
     * 判断是否存在任意一个 assistant message 带有 tool calls。
     * <p>
     * 找到即返回，适合用于 ANALYZE_RESPONSE 的快速分支判断。
     */
    public static boolean hasAnyToolCalls(OpenAiChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return false;
        }

        for (ChatChoice choice : response.getChoices()) {
            if (choice == null) {
                continue;
            }

            ChatResponse chatResponse = choice.getChatResponse();
            if (chatResponse == null || chatResponse.getMessages() == null || chatResponse.getMessages().isEmpty()) {
                continue;
            }

            for (Message message : chatResponse.getMessages()) {
                if (message instanceof AssistantResponseMessage assistantMessage
                        && assistantMessage.getToolCalls() != null
                        && !assistantMessage.getToolCalls().isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 收集所有 assistant message 中的全部 tool calls。
     * <p>
     * 适合用于 EXECUTE_TOOL 节点后续真正执行工具时遍历全部调用。
     */
    public static List<ToolCall> collectAllToolCalls(OpenAiChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolCall> result = new ArrayList<>();

        for (ChatChoice choice : response.getChoices()) {
            if (choice == null) {
                continue;
            }

            ChatResponse chatResponse = choice.getChatResponse();
            if (chatResponse == null || chatResponse.getMessages() == null || chatResponse.getMessages().isEmpty()) {
                continue;
            }

            for (Message message : chatResponse.getMessages()) {
                if (message instanceof AssistantResponseMessage assistantMessage
                        && assistantMessage.getToolCalls() != null
                        && !assistantMessage.getToolCalls().isEmpty()) {
                    result.addAll(assistantMessage.getToolCalls());
                }
            }
        }

        return result;
    }

    /**
     * 收集所有 assistant message 中的完整工具执行请求。
     * <p>
     * 保留 index / id / type / function 全部字段，
     * 后续执行层内部可自行决定取舍。
     */
    public static List<ToolExecutionRequest> collectExecutionRequests(OpenAiChatCompletionResponse response) {
        List<ToolCall> toolCalls = collectAllToolCalls(response);
        if (toolCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolExecutionRequest> result = new ArrayList<>(toolCalls.size());
        for (ToolCall toolCall : toolCalls) {
            if (toolCall == null || toolCall.getFunction() == null) {
                continue;
            }
            result.add(ToolExecutionRequest.from(toolCall));
        }
        return result;
    }

    /**
     * 收集全部 assistant 响应消息。
     * <p>
     * 后续如果 EXECUTE_TOOL 需要把原 assistant 消息回写到 workingMessages，
     * 可以直接复用这个方法。
     */
    public static List<AssistantResponseMessage> collectAssistantMessages(OpenAiChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return Collections.emptyList();
        }

        List<AssistantResponseMessage> result = new ArrayList<>();

        for (ChatChoice choice : response.getChoices()) {
            if (choice == null) {
                continue;
            }

            ChatResponse chatResponse = choice.getChatResponse();
            if (chatResponse == null || chatResponse.getMessages() == null || chatResponse.getMessages().isEmpty()) {
                continue;
            }

            for (Message message : chatResponse.getMessages()) {
                if (message instanceof AssistantResponseMessage assistantMessage) {
                    result.add(assistantMessage);
                }
            }
        }

        return result;
    }
}