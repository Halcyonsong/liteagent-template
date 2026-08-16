package io.github.halcyonsong.liteagent.provider.openai.agent.constant;

/**
 * OpenAI agent 链路内部使用的上下文属性键。
 */
public final class OpenAiAgentAttributes {

    public static final String PROVIDER_REQUEST = "openai.provider.request";
    public static final String RAW_REQUEST = "openai.raw.request";
    public static final String RAW_RESPONSE = "openai.raw.response";
    public static final String PROVIDER_RESPONSE = "openai.provider.response";

    /**
     * 当前轮解析出的工具执行请求列表。
     */
    public static final String TOOL_EXECUTION_REQUESTS = "openai.tool.execution.requests";

    private OpenAiAgentAttributes() {
    }
}