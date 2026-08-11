package io.github.halcyonsong.liteagent.provider.openai.support;

/**
 * OpenAI-compatible 接口地址解析工具。
 */
public final class OpenAiEndpointResolver {

    private OpenAiEndpointResolver() {
    }

    /**
     * 规范化 OpenAI-compatible chat completions 接口地址。
     * <p>
     * 若调用方未完整提供 /v1/chat/completions 后缀，则自动补全。
     *
     * @param baseUrl 用户提供的基础地址
     * @return 完整的 chat completions 接口地址
     */
    public static String resolveChatCompletionsEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }

        String normalized = trimTrailingSlash(baseUrl.trim());

        if (normalized.endsWith("/v1/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1/chat")) {
            return normalized + "/completions";
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    /**
     * 移除字符串末尾的斜杠。
     * <p>
     * 用于规范化 OpenAI-compatible 接口地址，确保路径无多余斜杠。
     * @param value 输入字符串
     * @return 处理后的字符串，末尾无斜杠
     */
    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}