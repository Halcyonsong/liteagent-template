package io.github.halcyonsong.liteagent.provider.openai.request.config;

import io.github.halcyonsong.liteagent.core.model.request.norm.BaseOptions;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible chat completions 扩展请求参数。封装 top_p、n、stop、penalty 等可选字段。
 */
@Getter
@Builder
public class OpenAiCompletionOptions implements BaseOptions {

    /** 温度参数，介于 0 和 2.0 之间。 */
    private final Double temperature;

    /** 最大生成 token 数量。 */
    private final Integer maxTokens;

    /** 核采样概率，介于 0 和 1 之间。 */
    private final Double topP;

    /** 生成的候选结果数量。 */
    private final Integer n;

    /** 停止生成序列，单个字符串或列表。 */
    private final Stop stop;

    /** 存在度惩罚，介于 -2.0 和 2.0 之间。 */
    private final Double presencePenalty;

    /** 频率惩罚，介于 -2.0 和 2.0 之间。 */
    private final Double frequencyPenalty;

    /** 指定返回格式，例如 {"type":"json_object"}。 */
    private final Map<String, Object> responseFormat;

    /**
     * OpenAI-compatible 协议中的 stop 参数包装类型。单个字符串或字符串列表。
     */
    @Getter
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

        /** 转换为 raw request 可用的值，String 或 List<String>。 */
        public Object toRawValue() {
            return isSingle() ? single : multiple;
        }
    }

}