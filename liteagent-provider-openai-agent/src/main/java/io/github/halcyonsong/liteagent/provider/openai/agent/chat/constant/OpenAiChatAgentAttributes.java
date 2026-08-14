package io.github.halcyonsong.liteagent.provider.openai.agent.chat.constant;

/**
 * OpenAI agent 编排过程中写入 ChatAgentContext.attributes 的键定义。
 * <p>
 * 这些键用于在不同步骤之间传递 provider-specific 中间产物。
 */
public final class OpenAiChatAgentAttributes {

    public static final String PROVIDER_REQUEST = "openai.provider.request";
    public static final String RAW_REQUEST = "openai.raw.request";
    public static final String RAW_RESPONSE = "openai.raw.response";
    public static final String PROVIDER_RESPONSE = "openai.provider.response";

    private OpenAiChatAgentAttributes() {
    }
}