package io.github.halcyonsong.liteagent.provider.openai.agent.constant;

/**
 * OpenAI agent 编排过程中写入 context 的属性键定义。
 * <p>
 * chat / stream 共用同一套键，避免重复维护。
 */
public final class OpenAiAgentAttributes {

    public static final String PROVIDER_REQUEST = "openai.provider.request";
    public static final String RAW_REQUEST = "openai.raw.request";
    public static final String RAW_RESPONSE = "openai.raw.response";
    public static final String PROVIDER_RESPONSE = "openai.provider.response";

    private OpenAiAgentAttributes() {
    }
}