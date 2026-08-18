package io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.state.StreamRoundState;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.support.OpenAiStreamToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiToolExecutionSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 执行当前轮流式响应中的全部工具调用，只把工具结果写入当前轮 pending 区，不直接修改 workingMessages。
 */
@Slf4j
public class OpenAiStreamExecuteToolStep implements StreamSyncStep {

    private final ToolExecutor toolExecutor;

    public OpenAiStreamExecuteToolStep() {
        this(new ReflectionToolExecutor());
    }

    public OpenAiStreamExecuteToolStep(ToolExecutor toolExecutor) {
        this.toolExecutor = Objects.requireNonNull(
                toolExecutor,
                "toolExecutor must not be null"
        );
    }

    @Override
    public StreamStepKey invoke(StreamAgentContext<?> context) {
        Objects.requireNonNull(context, "context must not be null");

        ToolRegistry registry = context.getToolRegistry();
        if (registry == null) {
            log.warn(
                    "Tool execution requested but registry missing. execId={}, round={}",
                    context.getExecutionId(),
                    context.currentRound().getRoundIndex()
            );

            context.setTerminationReason(
                    AgentTerminationReason.TOOL_ERROR
            );
            throw new IllegalStateException(
                    "Tool registry is required for stream tool execution"
            );
        }

        OpenAiStreamCompletionResponse response =
                OpenAiStreamToolCallSupport.getFinalResponse(context);

        List<ToolExecutionRequest> requests =
                OpenAiStreamToolCallSupport.collectExecutionRequests(response);

        log.debug(
                "Collected tool requests. execId={}, round={}, reqs={}",
                context.getExecutionId(),
                context.currentRound().getRoundIndex(),
                requests.size()
        );

        if (requests.isEmpty()) {
            return StreamStepKey.APPEND_MESSAGES;
        }

        StreamRoundState roundState = context.currentRound();

        try {
            roundState.appendPendingToolMessages(
                    OpenAiToolExecutionSupport.executeToMessages(requests, toolExecutor, registry)
            );
        } catch (Exception e) {
            roundState.clearPendingMessages();
            context.setTerminationReason(AgentTerminationReason.TOOL_ERROR);

            log.error(
                    "Failed to execute tools. execId={}, round={}, reqs={}",
                    context.getExecutionId(),
                    roundState.getRoundIndex(),
                    requests.size(),
                    e
            );

            throw e;
        }

        log.debug(
                "Executed tools. execId={}, round={}, toolMsgs={}",
                context.getExecutionId(),
                context.currentRound().getRoundIndex(),
                context.currentRound().getPendingToolMessages().size()
        );

        return StreamStepKey.APPEND_MESSAGES;
    }
}