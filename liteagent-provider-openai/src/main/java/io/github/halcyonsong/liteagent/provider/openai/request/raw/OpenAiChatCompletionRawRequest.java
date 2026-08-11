package io.github.halcyonsong.liteagent.provider.openai.request.raw;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat completions 原始请求体。
 * <p>
 * 该类字段与实际发送到远端接口的 JSON 请求结构一一对应，
 * 用于 provider 层发起 HTTP 调用，不直接作为 core 通用请求模型对外暴露。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatCompletionRawRequest {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<Map<String, Object>> messages;

    @JsonProperty("stream")
    private Boolean stream;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("n")
    private Integer n;

    /**
     * 停止生成序列，可以是单个字符串或字符串数组。
     */
    @JsonProperty("stop")
    private Object stop;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    /**
     * 指定模型输出格式，例如强制返回 JSON 对象。
     */
    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;

    public OpenAiChatCompletionRawRequest() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, Object>> messages) {
        this.messages = messages;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Object getStop() {
        return stop;
    }

    public void setStop(Object stop) {
        this.stop = stop;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Map<String, Object> getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(Map<String, Object> responseFormat) {
        this.responseFormat = responseFormat;
    }
}