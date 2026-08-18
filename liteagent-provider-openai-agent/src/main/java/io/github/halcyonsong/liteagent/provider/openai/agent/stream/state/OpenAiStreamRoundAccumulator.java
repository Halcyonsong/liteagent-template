package io.github.halcyonsong.liteagent.provider.openai.agent.stream.state;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChoice;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamDelta;
import io.github.halcyonsong.liteagent.core.model.tool.FunctionCall;
import io.github.halcyonsong.liteagent.core.model.tool.ToolCall;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiBaseResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.config.OpenAiUsage;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 单轮流式响应聚合器，把当前轮多个 chunk 聚合为最终响应。聚合时先按 choice.index 分组，再按 toolCall.index 聚合工具调用。
 */
@Getter
public class OpenAiStreamRoundAccumulator {

    /**
     * 最后一个非空的基础响应信息。
     */
    private OpenAiBaseResponse baseResponse;

    /**
     * 最后一个非空的 usage（不同供应商可能只在最后一个 chunk 返回或重复返回 usage）。
     */
    private OpenAiUsage usage;

    /**
     * 按 choice.index 保存当前轮的聚合结果，LinkedHashMap 保持模型返回 choice 的顺序。
     */
    private final Map<Integer, ChoiceAccumulator> choiceAccumulators = new LinkedHashMap<>();

    /**
     * 最后一个非空的结束原因。
     */
    private FinishReason finishReason;

    /**
     * 接收并聚合一个流式 chunk。
     */
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

        List<StreamChoice> choices = chunk.getChoices();
        if (choices == null || choices.isEmpty()) {
            return;
        }

        for (StreamChoice choice : choices) {
            if (choice == null) {
                continue;
            }

            Integer choiceIndex = choice.getIndex();
            if (choice.getIndex() == null) {
                continue;
            }

            ChoiceAccumulator choiceAccumulator =
                    choiceAccumulators.computeIfAbsent(
                            choiceIndex,
                            ChoiceAccumulator::new
                    );

            choiceAccumulator.accumulate(choice);

            if (choice.getFinishReason() != null) {
                this.finishReason = choice.getFinishReason();
            }
        }
    }

    public boolean hasBaseResponse() {
        return baseResponse != null;
    }

    /**
     * 判断当前是否已经收到至少一个 choice。
     */
    public boolean hasChoices() {
        return !choiceAccumulators.isEmpty();
    }

    /**
     * 获取已经聚合的 choice，返回实时构造的快照，不暴露内部可变 Map。
     */
    public List<StreamChoice> getChoices() {
        List<StreamChoice> result = new ArrayList<>(choiceAccumulators.size());

        for (ChoiceAccumulator accumulator : choiceAccumulators.values()) {
            result.add(accumulator.toChoice());
        }

        return Collections.unmodifiableList(result);
    }

    public boolean hasData() {
        return hasBaseResponse() || hasChoices() || usage != null;
    }

    public OpenAiStreamCompletionResponse toFinalResponse() {
        if (baseResponse == null) {
            throw new IllegalStateException(
                    "Cannot build final stream response without baseResponse"
            );
        }

        return new OpenAiStreamCompletionResponse(
                baseResponse,
                getChoices(),
                usage
        );
    }

    public OpenAiStreamCompletionResponse tryToFinalResponse() {
        if (baseResponse == null) {
            return null;
        }

        return new OpenAiStreamCompletionResponse(
                baseResponse,
                getChoices(),
                usage
        );
    }

    /**
     * 单个 choice 的聚合状态。
     */
    private static final class ChoiceAccumulator {

        private final Integer index;

        private String role;
        private final StringBuilder content = new StringBuilder();
        private final StringBuilder reasoningContent = new StringBuilder();

        /**
         * 按 toolCall.index 保存工具调用聚合状态。
         */
        private final Map<Integer, ToolCallAccumulator> toolCalls =
                new LinkedHashMap<>();

        private FinishReason finishReason;

        private ChoiceAccumulator(Integer index) {
            this.index = index;
        }

        private void accumulate(StreamChoice choice) {
            if (choice.getFinishReason() != null) {
                this.finishReason = choice.getFinishReason();
            }

            StreamDelta delta = choice.getDelta();
            if (delta == null) {
                return;
            }

            if (delta.getRole() != null) {
                this.role = delta.getRole();
            }

            if (delta.getContent() != null) {
                content.append(delta.getContent());
            }

            if (delta.getReasoningContent() != null) {
                reasoningContent.append(delta.getReasoningContent());
            }

            if (delta.getToolCalls() == null || delta.getToolCalls().isEmpty()) {
                return;
            }

            for (ToolCall toolCall : delta.getToolCalls()) {
                if (toolCall == null) {
                    continue;
                }

                Integer toolCallIndex = toolCall.getIndex();

                ToolCallAccumulator toolCallAccumulator =
                        toolCalls.computeIfAbsent(
                                toolCallIndex,
                                ToolCallAccumulator::new
                        );

                toolCallAccumulator.accumulate(toolCall);
            }
        }

        private StreamChoice toChoice() {
            List<ToolCall> aggregatedToolCalls = new ArrayList<>(
                    toolCalls.size()
            );

            for (ToolCallAccumulator toolCall : toolCalls.values()) {
                aggregatedToolCalls.add(toolCall.toToolCall());
            }

            StreamDelta delta = new StreamDelta(
                    role,
                    content.isEmpty() ? null : content.toString(),
                    reasoningContent.isEmpty() ? null : reasoningContent.toString(),
                    aggregatedToolCalls
            );

            return new StreamChoice(index, delta, finishReason);
        }
    }

    /**
     * 单个工具调用的增量聚合状态。
     */
    private static final class ToolCallAccumulator {

        private final Integer index;

        private String id;
        private String type;
        private String functionName;

        /**
         * function.arguments 可能跨多个 chunk 返回，必须追加而不是覆盖。
         */
        private final StringBuilder arguments = new StringBuilder();

        private ToolCallAccumulator(Integer index) {
            this.index = index;
        }

        private void accumulate(ToolCall toolCall) {
            if (toolCall.getId() != null && !toolCall.getId().isBlank()) {
                this.id = toolCall.getId();
            }

            if (toolCall.getType() != null && !toolCall.getType().isBlank()) {
                this.type = toolCall.getType();
            }

            FunctionCall function = toolCall.getFunction();
            if (function == null) {
                return;
            }

            if (function.getName() != null && !function.getName().isBlank()) {
                this.functionName = function.getName();
            }

            if (function.getArguments() != null) {
                arguments.append(function.getArguments());
            }
        }

        private ToolCall toToolCall() {
            FunctionCall function = new FunctionCall(
                    functionName,
                    arguments.isEmpty() ? null : arguments.toString()
            );

            return new ToolCall(index, id, type, function);
        }
    }
}