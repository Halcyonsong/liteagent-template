package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.type.AssistantResponseMessage;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.model.ToolExecutionRequest;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiToolCallSupport;
import io.github.halcyonsong.liteagent.provider.openai.agent.support.OpenAiToolExecutionSupport;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 执行当前轮 chat 响应中的全部工具调用，只填充 pending assistant/tool 消息，不直接写入 workingMessages。
 */
@Slf4j
public class OpenAiChatExecuteToolStep implements ChatStep {

    private final ToolExecutor toolExecutor;

    public OpenAiChatExecuteToolStep() {
        this(new ReflectionToolExecutor());
    }

    public OpenAiChatExecuteToolStep(ToolExecutor toolExecutor) {
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
    }

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            log.warn(
                    "Tool execution requested but response missing. execId={}, iter={}",
                    context.getExecutionId(),
                    context.getIteration()
            );
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        ToolRegistry toolRegistry = context.getToolRegistry();

        if (toolRegistry == null) {
            log.warn(
                    "Tool execution requested but registry missing. execId={}, iter={}",
                    context.getExecutionId(),
                    context.getIteration()
            );
            context.setTerminationReason(AgentTerminationReason.TOOL_ERROR);
            throw new IllegalStateException("Tool registry is required for chat tool execution");
        }

        List<ToolExecutionRequest> executionRequests = OpenAiToolCallSupport.collectExecutionRequests(response);

        log.debug(
                "Collected tool requests. execId={}, iter={}, reqs={}",
                context.getExecutionId(),
                context.getIteration(),
                executionRequests.size()
        );

        if (executionRequests.isEmpty()) {
            return ChatStepKey.BUILD_RESULT;
        }

        context.setAttribute(
                OpenAiAgentAttributes.TOOL_EXECUTION_REQUESTS,
                executionRequests
        );

        List<AssistantResponseMessage> assistantMessages =
                OpenAiToolCallSupport.collectAssistantMessages(response);

        context.clearPendingMessages();
        context.appendPendingAssistantMessages(assistantMessages);

        try {
            context.appendPendingToolMessages(
                    OpenAiToolExecutionSupport.executeToMessages(executionRequests, toolExecutor, toolRegistry)
            );
        } catch (Exception e) {
            context.clearPendingMessages();
            context.setTerminationReason(AgentTerminationReason.TOOL_ERROR);

            log.error(
                    "Failed to execute tools. execId={}, iter={}, reqs={}",
                    context.getExecutionId(),
                    context.getIteration(),
                    executionRequests.size(),
                    e
            );

            throw e;
        }

        log.debug(
                "Executed tools. execId={}, iter={}, msgs={}, toolMsgs={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getPendingAssistantMessages().size(),
                context.getPendingToolMessages().size()
        );

        return ChatStepKey.APPEND_MESSAGES;
    }
}