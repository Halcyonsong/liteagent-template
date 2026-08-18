package io.github.halcyonsong.liteagent.provider.openai.agent.chat.step.response;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStep;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenAiChatBuildResultStep implements ChatStep {

    @Override
    public ChatStepKey invoke(ChatAgentContext context) {
        OpenAiChatCompletionResponse response = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_RESPONSE,
                OpenAiChatCompletionResponse.class
        );

        if (response == null) {
            throw new IllegalStateException(
                    "Missing provider response in agent context"
            );
        }

        context.setResult(response);

        if (context.getTerminationReason() == null) {
            context.setTerminationReason(
                    AgentTerminationReason.COMPLETED
            );
        }

        log.debug(
                "Built result. execId={}, iter={}, reason={}, respId={}",
                context.getExecutionId(),
                context.getIteration(),
                context.getTerminationReason(),
                response.getBaseResponse() == null ? null : response.getBaseResponse().getId()
        );

        return ChatStepKey.END;
    }
}