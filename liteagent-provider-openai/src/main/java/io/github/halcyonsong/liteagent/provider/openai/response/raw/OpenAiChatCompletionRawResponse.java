package io.github.halcyonsong.liteagent.provider.openai.response.raw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat completions 原始响应体。字段与远端返回的 JSON 结构一一对应。
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiChatCompletionRawResponse implements JsonSerializable {

    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("created")
    private Long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<RawChoice> choices;

    @JsonProperty("usage")
    private RawUsage usage;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    /** 某些供应商返回的扩展字段，结构不统一，按原始 map 接收。 */
    @JsonProperty("base_resp")
    private Map<String, Object> baseResp;

    @Override
    public String toString() {
        return "OpenAiChatCompletionRawResponse{" +
                "id='" + id + '\'' +
                ", object='" + object + '\'' +
                ", model='" + model + '\'' +
                ", choiceCount=" + (choices == null ? 0 : choices.size()) +
                ", usage=" + usage +
                '}';
    }

    /** 原始响应中的单条 choice 结构。普通响应用 message，流式 chunk 用 delta。 */
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawChoice implements JsonSerializable {

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private RawMessage message;

        @JsonProperty("delta")
        private RawMessage delta;

        @JsonProperty("finish_reason")
        private String finishReason;

    }

    /** 原始响应中的 message/delta 结构，普通响应和流式 chunk 均可复用。 */
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawMessage implements JsonSerializable {

        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;

        @JsonProperty("tool_calls")
        private List<RawToolCall> toolCalls;

    }

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawToolCall implements JsonSerializable {

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("function")
        private RawFunction function;

    }

    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawFunction implements JsonSerializable {

        @JsonProperty("name")
        private String name;

        @JsonProperty("arguments")
        private String arguments;

    }

    /** 原始响应中的 token 用量结构。 */
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawUsage implements JsonSerializable {

        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;

        @JsonProperty("completion_tokens_details")
        private Map<String, Object> completionTokensDetails;

        @JsonProperty("prompt_tokens_details")
        private Map<String, Object> promptTokensDetails;

        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;

        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;

    }
}