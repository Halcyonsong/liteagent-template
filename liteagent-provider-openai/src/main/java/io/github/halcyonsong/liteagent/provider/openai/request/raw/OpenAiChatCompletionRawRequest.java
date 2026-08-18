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
 * OpenAI-compatible chat completions 原始请求体。字段与远端 JSON 请求结构一一对应。
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

    /** 流式请求选项，仅在 stream=true 时生效。目前用于开启 usage 统计返回。 */
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

    /** 停止生成序列，单个字符串或字符串数组。 */
    @JsonProperty("stop")
    private Object stop;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    /** 指定模型输出格式，例如强制返回 JSON 对象。 */
    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;

    /** 工具定义列表，声明本次请求可用的工具。 */
    @JsonProperty("tools")
    private List<Map<String, Object>> tools;

    @JsonProperty("tool_choice")
    private Object toolChoice;

    public OpenAiChatCompletionRawRequest() {
    }

}