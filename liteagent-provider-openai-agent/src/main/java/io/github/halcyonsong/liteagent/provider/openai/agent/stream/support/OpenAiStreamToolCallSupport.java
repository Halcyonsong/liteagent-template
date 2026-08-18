package io.github.halcyonsong.liteagent.provider.openai.agent.stream.support;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 流式响应工具调用辅助类。
 *
 * <p>该类只读取当前轮已经聚合完成的 finalResponse，
 * 不读取单个增量 chunk。</p>
 */
public final class OpenAiStreamToolCallSupport {

    private OpenAiStreamToolCallSupport() {
    }

    /**
     * 获取当前轮聚合后的 provider response。
     */
    public static OpenAiStreamCompletionResponse getFinalResponse(
            StreamAgentContext<?> context
    ) {
        Objects.requireNonNull(context, "context must not be null");

        StreamRoundState roundState = context.currentRound();
        Object finalResponse = roundState.getFinalResponse();

        if (!(finalResponse instanceof OpenAiStreamCompletionResponse response)) {
            throw new IllegalStateException(
                    "Missing aggregated OpenAI stream response in current round"
            );
        }

        return response;
    }

    /**
     * 判断当前轮是否存在工具调用。
     */
    public static boolean hasToolCalls(
            OpenAiStreamCompletionResponse response
    ) {
        return !collectToolCalls(response).isEmpty();
    }

    /**
     * 收集当前轮所有 choice 中已经聚合完成的工具调用。
     */
    public static List<ToolCall> collectToolCalls(
            OpenAiStreamCompletionResponse response
    ) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolCall> result = new ArrayList<>();

        for (StreamChoice choice : response.getChoices()) {
            if (choice == null) {
                continue;
            }

            StreamDelta delta = choice.getDelta();
            if (delta == null
                    || delta.getToolCalls() == null
                    || delta.getToolCalls().isEmpty()) {
                continue;
            }

            for (ToolCall toolCall : delta.getToolCalls()) {
                if (toolCall != null && toolCall.getFunction() != null) {
                    result.add(toolCall);
                }
            }
        }

        return result;
    }

    /**
     * 将当前轮聚合结果转换为工具执行请求。
     */
    public static List<ToolExecutionRequest> collectExecutionRequests(OpenAiStreamCompletionResponse response) {
        List<ToolCall> toolCalls = collectToolCalls(response);

        if (toolCalls.isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolExecutionRequest> result = new ArrayList<>(toolCalls.size());

        for (ToolCall toolCall : toolCalls) {
            result.add(ToolExecutionRequest.from(toolCall));
        }

        return result;
    }

    /**
     * 将当前轮聚合结果转换为 assistant 消息。
     *
     * <p>工具调用轮次必须先写入 assistant tool_calls，
     * 然后再写入对应的 tool 消息。</p>
     */
    public static List<AssistantResponseMessage> collectAssistantMessages(OpenAiStreamCompletionResponse response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {
            return Collections.emptyList();
        }

        List<AssistantResponseMessage> result = new ArrayList<>();

        for (StreamChoice choice : response.getChoices()) {
            if (choice == null || choice.getDelta() == null) {
                continue;
            }

            StreamDelta delta = choice.getDelta();

            String content = delta.getContent() == null
                    ? ""
                    : delta.getContent();

            result.add(new AssistantResponseMessage(
                    content,
                    delta.getReasoningContent(),
                    delta.getToolCalls()
            ));
        }

        return result;
    }
}