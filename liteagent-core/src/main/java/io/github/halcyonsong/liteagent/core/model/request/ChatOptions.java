package io.github.halcyonsong.liteagent.core.model.request;

/**
 * 统一的聊天调用可选参数。
 * <p>
 * 该对象用于存放跨供应商相对稳定的基础生成控制项，
 * 例如是否流式返回、温度参数以及最大生成 token 数。
 * 供应商特有扩展参数不应放入该类。
 */
public class ChatOptions {

    /**
     * 是否启用流式返回。当前普通调用场景下通常为 false。
     */
    private final Boolean stream;

    /**
     * 采样温度，用于控制输出随机性。
     */
    private final Double temperature;

    /**
     * 最大生成 token 数限制。
     */
    private final Integer maxTokens;

    private ChatOptions(Builder builder) {
        this.stream = builder.stream;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Boolean stream;
        private Double temperature;
        private Integer maxTokens;

        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public ChatOptions build() {
            return new ChatOptions(this);
        }
    }
}