package io.github.halcyonsong.liteagent.core.model.response;

/**
 * 模型调用的 token 用量信息。
 * <p>
 * 该对象用于记录一次请求中的提示词 token 数、生成 token 数以及总 token 数。
 */
public class Usage {

    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;

    public Usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }
}