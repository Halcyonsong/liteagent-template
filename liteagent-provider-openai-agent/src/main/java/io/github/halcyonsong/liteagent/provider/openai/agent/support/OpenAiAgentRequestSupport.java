package io.github.halcyonsong.liteagent.provider.openai.agent.support;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.provider.openai.agent.constant.OpenAiAgentAttributes;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;

/**
 * OpenAI provider-agent 请求读取辅助类，统一封装 chat / stream 步骤中的 provider/raw request 读取、空值校验与 endpoint/apiKey 解析。
 */
public final class OpenAiAgentRequestSupport {

    private OpenAiAgentRequestSupport() {
    }

    public static OpenAiChatCompletionRequest requireProviderRequest(ChatAgentContext context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        if (providerRequest == null) {
            throw new IllegalStateException("Missing provider request in agent context");
        }
        return providerRequest;
    }

    public static OpenAiChatCompletionRawRequest requireRawRequest(ChatAgentContext context) {
        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );
        if (rawRequest == null) {
            throw new IllegalStateException("Missing raw request in agent context");
        }
        return rawRequest;
    }

    public static OpenAiChatCompletionRequest requireProviderRequest(StreamAgentContext<?> context) {
        OpenAiChatCompletionRequest providerRequest = context.getAttribute(
                OpenAiAgentAttributes.PROVIDER_REQUEST,
                OpenAiChatCompletionRequest.class
        );
        if (providerRequest == null) {
            throw new IllegalStateException("Missing provider request in stream context");
        }
        return providerRequest;
    }

    public static OpenAiChatCompletionRawRequest requireRawRequest(StreamAgentContext<?> context) {
        OpenAiChatCompletionRawRequest rawRequest = context.getAttribute(
                OpenAiAgentAttributes.RAW_REQUEST,
                OpenAiChatCompletionRawRequest.class
        );
        if (rawRequest == null) {
            throw new IllegalStateException("Missing raw request in stream context");
        }
        return rawRequest;
    }

    public static String resolveEndpoint(OpenAiChatCompletionRequest providerRequest) {
        return OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                providerRequest.getBaseRequest().getBaseUrl()
        );
    }

    public static String resolveApiKey(OpenAiChatCompletionRequest providerRequest) {
        return providerRequest.getBaseRequest().getApiKey();
    }
}