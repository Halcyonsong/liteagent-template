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

import java.util.List;
import java.util.Objects;

/**
 * 流式工具执行步骤。
 *
 * <p>该步骤只执行工具并将结果放入当前轮暂存区，
 * 不直接修改 workingMessages。</p>
 */
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

        if (requests.isEmpty()) {
            return StreamStepKey.APPEND_MESSAGES;
        }

        StreamRoundState roundState = context.currentRound();

        try {
            roundState.appendPendingToolMessages(
                    OpenAiToolExecutionSupport.executeToMessages(requests, toolExecutor, registry)
            );
        } catch (Exception error) {
            roundState.clearPendingMessages();
            context.setTerminationReason(AgentTerminationReason.TOOL_ERROR);
            throw error;
        }

        return StreamStepKey.APPEND_MESSAGES;
    }
}