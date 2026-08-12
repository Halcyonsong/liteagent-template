package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.BaseOptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible chat completions 扩展请求参数。
 * <p>
 * 该对象用于封装不进入 core 通用请求模型、但可被 OpenAI-compatible 协议支持的可选字段，
 * 例如 top_p、n、stop、presence_penalty、frequency_penalty 和 response_format。
 */
public class OpenAiCompletionOptions implements BaseOptions {

    /**
     * 温度参数，介于 0 和 2.0 之间。
     * 控制生成结果的随机性和多样性。
     */
    private final Double temperature;

    /**
     * 最大生成 token 数量。
     */
    private final Integer maxTokens;

    /**
     * 核采样概率，介于 0 和 1 之间。与 temperature 类似，是另一种控制输出随机性的方法。
     */
    private final Double topP;

    /**
     * 生成的候选结果数量。
     */
    private final Integer n;

    /**
     * 停止生成序列。
     * 可以是单个字符串，也可以是字符串列表。
     */
    private final Stop stop;

    /**
     * 存在度惩罚，介于 -2.0 和 2.0 之间。
     * 用于调整模型对新生成的 token 的奖励，以控制生成结果的多样性。
     */
    private final Double presencePenalty;

    /**
     * 频率惩罚，介于 -2.0 和 2.0 之间。
     * 用于调整模型对新生成的 token 的奖励，以控制生成结果的多样性。
     */
    private final Double frequencyPenalty;

    /**
     * 指定返回格式，例如 {"type":"json_object"}。
     */
    private final Map<String, Object> responseFormat;

    private OpenAiCompletionOptions(Builder builder) {
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.topP = builder.topP;
        this.n = builder.n;
        this.stop = builder.stop;
        this.presencePenalty = builder.presencePenalty;
        this.frequencyPenalty = builder.frequencyPenalty;
        this.responseFormat = builder.responseFormat;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public Integer getN() {
        return n;
    }

    public Stop getStop() {
        return stop;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public Map<String, Object> getResponseFormat() {
        return responseFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
        private Integer n;
        private Stop stop;
        private Double presencePenalty;
        private Double frequencyPenalty;
        private Map<String, Object> responseFormat;

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder n(Integer n) {
            this.n = n;
            return this;
        }

        public Builder stop(Stop stop) {
            this.stop = stop;
            return this;
        }

        public Builder stop(List<String> stop) {
            this.stop = Stop.of(stop);
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder responseFormat(Map<String, Object> responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        public OpenAiCompletionOptions build() {
            return new OpenAiCompletionOptions(this);
        }
    }

    /**
     * OpenAI-compatible 协议中的 stop 参数包装类型。
     * <p>
     * 该参数可以是单个停止序列字符串，也可以是多个停止序列字符串列表。
     */
    public static class Stop {

        private final String single;
        private final List<String> multiple;

        private Stop(String single, List<String> multiple) {
            this.single = single;
            this.multiple = multiple == null ? null : List.copyOf(multiple);
        }

        public static Stop of(String stop) {
            Objects.requireNonNull(stop, "stop must not be null");
            return new Stop(stop, null);
        }

        public static Stop of(List<String> stop) {
            Objects.requireNonNull(stop, "stop must not be null");
            if (stop.isEmpty()) {
                throw new IllegalArgumentException("stop must not be empty");
            }
            return new Stop(null, stop);
        }

        public boolean isSingle() {
            return single != null;
        }

        public boolean isMultiple() {
            return multiple != null;
        }

        public String getSingle() {
            return single;
        }

        public List<String> getMultiple() {
            return multiple;
        }

        /**
         * 转换为 OpenAI-compatible raw request 可直接使用的值。
         *
         * @return String 或 List<String>
         */
        public Object toRawValue() {
            return isSingle() ? single : multiple;
        }
    }

}