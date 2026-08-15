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

import java.util.List;
import java.util.Objects;

/**
 * OpenAI 工具执行步骤。
 * <p>
 * 当前实现：
 * 1. 解析本轮全部工具调用
 * 2. 逐个执行工具
 * 3. 将 assistant/tool 消息写入 pending 列表
 * 4. 返回 APPEND_MESSAGES，由下一节点统一写入 workingMessages
 */
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

        ToolRegistry toolRegistry = context.getToolRegistry();

        if (response == null) {
            context.setTerminationReason(AgentTerminationReason.MODEL_ERROR);
            return ChatStepKey.END;
        }

        if (toolRegistry == null) {
            context.setTerminationReason(AgentTerminationReason.TOOL_ERROR);
            return ChatStepKey.END;
        }

        List<ToolExecutionRequest> executionRequests =
                OpenAiToolCallSupport.collectExecutionRequests(response);

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
            throw e;
        }

        return ChatStepKey.APPEND_MESSAGES;
    }
}