package io.github.halcyonsong.liteagent.provider.openai.request.raw;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible chat completions 原始请求体。
 * <p>
 * 该类字段与实际发送到远端接口的 JSON 请求结构一一对应，
 * 用于 provider 层发起 HTTP 调用，不直接作为 core 通用请求模型对外暴露。
 */
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatCompletionRawRequest implements JsonSerializable {

    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<Map<String, Object>> messages;

    @JsonProperty("stream")
    private Boolean stream;

    /**
     * 流式请求选项。
     * <p>
     * 仅在 stream=true 时生效，用于控制流式响应的附加行为。
     * 目前用于开启 usage 统计返回。
     */
    @JsonProperty("stream_options")
    private Map<String, Object> streamOptions;

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

    /**
     * 工具定义列表。
     * <p>
     * 该字段用于向 OpenAI-compatible 协议声明本次请求可用的工具。
     * 具体结构后续可再细化，目前先保留为通用 JSON 容器。
     */
    @JsonProperty("tools")
    private List<Map<String, Object>> tools;

    @JsonProperty("tool_choice")
    private Object toolChoice;

    public OpenAiChatCompletionRawRequest() {
    }

}