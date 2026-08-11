package io.github.halcyonsong.liteagent.provider.openai.support;

import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;

/**
 * OpenAI-compatible message/delta 统一读取工具。
 */
public final class OpenAiMessageResolver {

    private OpenAiMessageResolver() {
    }

    /**
     * 统一获取当前 choice 中实际承载内容的消息体。
     * <p>
     * 普通响应优先返回 message；
     * 流式响应返回 delta。
     */
    public static OpenAiChatCompletionRawResponse.RawMessage resolveMessage(
            OpenAiChatCompletionRawResponse.RawChoice choice) {

        if (choice == null) {
            return null;
        }
        if (choice.getMessage() != null) {
            return choice.getMessage();
        }
        return choice.getDelta();
    }

    /**
     * 当前 choice 是否包含实际消息载荷。
     */
    public static boolean hasMessagePayload(OpenAiChatCompletionRawResponse.RawChoice choice) {
        return resolveMessage(choice) != null;
    }
}