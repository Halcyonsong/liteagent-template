package io.github.halcyonsong.liteagent.provider.openai.response.config;

import io.github.halcyonsong.liteagent.core.model.response.norm.Usage;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * OpenAI-compatible provider 专属 usage，在 core 统一 usage 之外保留扩展字段。
 */
@Getter
@ToString
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

}