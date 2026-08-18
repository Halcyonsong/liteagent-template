package io.github.halcyonsong.liteagent.provider.openai.support;

/**
 * OpenAI-compatible 接口地址解析工具。
 */
public final class OpenAiEndpointResolver {

    private OpenAiEndpointResolver() {
    }

    /**
     * 规范化 chat completions 接口地址，自动补全 /v1/chat/completions 后缀。
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

    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}