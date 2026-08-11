package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.model.response.Usage;

import java.util.Map;

/**
 * OpenAI-compatible provider 专属 usage。
 * <p>
 * 在 core 统一 usage 之外，额外保留 provider 特有或扩展字段。
 */
public class OpenAiUsage extends Usage {

    private final Map<String, Object> completionTokensDetails;
    private final Map<String, Object> promptTokensDetails;
    private final Integer promptCacheHitTokens;
    private final Integer promptCacheMissTokens;

    public OpenAiUsage(Integer promptTokens,
                       Integer completionTokens,
                       Integer totalTokens,
                       Map<String, Object> completionTokensDetails,
                       Map<String, Object> promptTokensDetails,
                       Integer promptCacheHitTokens,
                       Integer promptCacheMissTokens) {
        super(promptTokens, completionTokens, totalTokens);
        this.completionTokensDetails = completionTokensDetails;
        this.promptTokensDetails = promptTokensDetails;
        this.promptCacheHitTokens = promptCacheHitTokens;
        this.promptCacheMissTokens = promptCacheMissTokens;
    }

    public Map<String, Object> getCompletionTokensDetails() {
        return completionTokensDetails;
    }

    public Map<String, Object> getPromptTokensDetails() {
        return promptTokensDetails;
    }

    public Integer getPromptCacheHitTokens() {
        return promptCacheHitTokens;
    }

    public Integer getPromptCacheMissTokens() {
        return promptCacheMissTokens;
    }

    @Override
    public String toString() {
        return "OpenAiUsage{" +
                "promptTokens=" + getPromptTokens() +
                ", completionTokens=" + getCompletionTokens() +
                ", totalTokens=" + getTotalTokens() +
                ", promptCacheHitTokens=" + promptCacheHitTokens +
                ", promptCacheMissTokens=" + promptCacheMissTokens +
                '}';
    }
}