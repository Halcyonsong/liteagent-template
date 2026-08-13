package io.github.halcyonsong.liteagent.provider.openai.request.advisor;

import io.github.halcyonsong.liteagent.core.model.request.RequestAdvisor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.tool.OpenAiToolChoice;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;

import java.util.Objects;

/**
 * tool_choice 请求增强器。
 */
public class OpenAiToolChoiceAdvisor implements RequestAdvisor<OpenAiChatCompletionRequest, OpenAiChatCompletionRawRequest> {

    private final OpenAiToolChoice toolChoice;

    public OpenAiToolChoiceAdvisor(OpenAiToolChoice toolChoice) {
        this.toolChoice = Objects.requireNonNull(toolChoice, "toolChoice must not be null");
    }

    @Override
    public void enhance(OpenAiChatCompletionRequest request,
                        OpenAiChatCompletionRawRequest rawRequest) {
        rawRequest.setToolChoice(toolChoice.toRawValue());
    }
}