package io.github.halcyonsong.liteagent.provider.openai.response.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI-compatible chat completions 原始响应体。
 * <p>
 * 该类字段与远端返回的 JSON 结构一一对应，
 * 用于接收原始协议响应，并在后续映射为 provider 包装响应对象。
 */
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

    /**
     * 原始响应中的单条 choice 结构。
     */
    public static class RawChoice {

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("message")
        private RawMessage message;

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

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    /**
     * 原始响应中的 message 结构。
     */
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
    }

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
    }

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
    }
}