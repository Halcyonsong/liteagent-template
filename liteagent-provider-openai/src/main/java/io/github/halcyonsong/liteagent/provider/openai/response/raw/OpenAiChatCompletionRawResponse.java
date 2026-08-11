package io.github.halcyonsong.liteagent.provider.openai.response.raw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat completions 原始响应体。
 * <p>
 * 该类字段与远端返回的 JSON 结构一一对应，
 * 用于接收原始协议响应，并在后续映射为 provider 包装响应对象。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiChatCompletionRawResponse {

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

    /**
     * 某些 OpenAI-compatible 供应商可能返回的扩展字段。
     * 结构不统一，先按原始 map 接收。
     */
    @JsonProperty("base_resp")
    private Map<String, Object> baseResp;

    public OpenAiChatCompletionRawResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<RawChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<RawChoice> choices) {
        this.choices = choices;
    }

    public RawUsage getUsage() {
        return usage;
    }

    public void setUsage(RawUsage usage) {
        this.usage = usage;
    }

    public String getSystemFingerprint() {
        return systemFingerprint;
    }

    public void setSystemFingerprint(String systemFingerprint) {
        this.systemFingerprint = systemFingerprint;
    }

    public Map<String, Object> getBaseResp() {
        return baseResp;
    }

    public void setBaseResp(Map<String, Object> baseResp) {
        this.baseResp = baseResp;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

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

    /**
     * 原始响应中的单条 choice 结构。
     * <p>
     * 普通响应使用 message；
     * 流式 chunk 使用 delta。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawChoice {

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private RawMessage message;

        @JsonProperty("delta")
        private RawMessage delta;

        @JsonProperty("finish_reason")
        private String finishReason;

        public RawChoice() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public RawMessage getMessage() {
            return message;
        }

        public void setMessage(RawMessage message) {
            this.message = message;
        }

        public RawMessage getDelta() {
            return delta;
        }

        public void setDelta(RawMessage delta) {
            this.delta = delta;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public String toJson() {
            return JsonSupport.toJson(this);
        }

        @Override
        public String toString() {
            return "RawChoice{" +
                    "index=" + index +
                    ", hasMessage=" + (message != null) +
                    ", hasDelta=" + (delta != null) +
                    ", finishReason='" + finishReason + '\'' +
                    '}';
        }
    }

    /**
     * 原始响应中的 message/delta 结构。
     * <p>
     * 普通响应和流式 chunk 均可复用该结构。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawMessage {

        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;

        @JsonProperty("tool_calls")
        private List<RawToolCall> toolCalls;

        public RawMessage() {
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getReasoningContent() {
            return reasoningContent;
        }

        public void setReasoningContent(String reasoningContent) {
            this.reasoningContent = reasoningContent;
        }

        public List<RawToolCall> getToolCalls() {
            return toolCalls;
        }

        public void setToolCalls(List<RawToolCall> toolCalls) {
            this.toolCalls = toolCalls;
        }

        public String toJson() {
            return JsonSupport.toJson(this);
        }

        @Override
        public String toString() {
            return "RawMessage{" +
                    "role='" + role + '\'' +
                    ", content='" + content + '\'' +
                    ", reasoningContent='" + reasoningContent + '\'' +
                    ", toolCallCount=" + (toolCalls == null ? 0 : toolCalls.size()) +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawToolCall {

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("function")
        private RawFunction function;

        public RawToolCall() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public RawFunction getFunction() {
            return function;
        }

        public void setFunction(RawFunction function) {
            this.function = function;
        }

        public String toJson() {
            return JsonSupport.toJson(this);
        }

        @Override
        public String toString() {
            return "RawToolCall{" +
                    "index=" + index +
                    ", id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", function=" + function +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawFunction {

        @JsonProperty("name")
        private String name;

        @JsonProperty("arguments")
        private String arguments;

        public RawFunction() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArguments() {
            return arguments;
        }

        public void setArguments(String arguments) {
            this.arguments = arguments;
        }

        public String toJson() {
            return JsonSupport.toJson(this);
        }

        @Override
        public String toString() {
            return "RawFunction{" +
                    "name='" + name + '\'' +
                    ", arguments='" + arguments + '\'' +
                    '}';
        }
    }

    /**
     * 原始响应中的 token 用量结构。
     */
    public static class RawUsage {

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

        public RawUsage() {
        }

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }

        public Map<String, Object> getCompletionTokensDetails() {
            return completionTokensDetails;
        }

        public void setCompletionTokensDetails(Map<String, Object> completionTokensDetails) {
            this.completionTokensDetails = completionTokensDetails;
        }

        public Map<String, Object> getPromptTokensDetails() {
            return promptTokensDetails;
        }

        public void setPromptTokensDetails(Map<String, Object> promptTokensDetails) {
            this.promptTokensDetails = promptTokensDetails;
        }

        public Integer getPromptCacheHitTokens() {
            return promptCacheHitTokens;
        }

        public void setPromptCacheHitTokens(Integer promptCacheHitTokens) {
            this.promptCacheHitTokens = promptCacheHitTokens;
        }

        public Integer getPromptCacheMissTokens() {
            return promptCacheMissTokens;
        }

        public void setPromptCacheMissTokens(Integer promptCacheMissTokens) {
            this.promptCacheMissTokens = promptCacheMissTokens;
        }

        public String toJson() {
            return JsonSupport.toJson(this);
        }

        @Override
        public String toString() {
            return "RawUsage{" +
                    "promptTokens=" + promptTokens +
                    ", completionTokens=" + completionTokens +
                    ", totalTokens=" + totalTokens +
                    ", promptCacheHitTokens=" + promptCacheHitTokens +
                    ", promptCacheMissTokens=" + promptCacheMissTokens +
                    '}';
        }
    }
}