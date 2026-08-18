package io.github.halcyonsong.liteagent.agent.chat.step;

/**
 * 同步 chat 编排步骤标识。内置常量覆盖标准编排流程，也支持通过 {@link #of(String)} 创建自定义 key。
 */
public interface ChatStepKey {

    /**
     * 步骤标识名称，用于日志、调试和相等性判断。
     */
    String name();

    // ─── 内置步骤 ───────────────────────────────────────────────

    ChatStepKey BEGIN = of("BEGIN");
    ChatStepKey INIT_WORKING_MESSAGES = of("INIT_WORKING_MESSAGES");
    ChatStepKey INIT_TOOL_REGISTRY = of("INIT_TOOL_REGISTRY");
    ChatStepKey MAP_REQUEST = of("MAP_REQUEST");
    ChatStepKey ENHANCE_REQUEST = of("ENHANCE_REQUEST");
    ChatStepKey SEND_REQUEST = of("SEND_REQUEST");
    ChatStepKey MAP_RESPONSE = of("MAP_RESPONSE");
    ChatStepKey ENHANCE_RESPONSE = of("ENHANCE_RESPONSE");
    ChatStepKey ANALYZE_RESPONSE = of("ANALYZE_RESPONSE");
    ChatStepKey EXECUTE_TOOL = of("EXECUTE_TOOL");
    ChatStepKey APPEND_MESSAGES = of("APPEND_MESSAGES");
    ChatStepKey BUILD_RESULT = of("BUILD_RESULT");
    ChatStepKey END = of("END");

    // ─── 自定义 key 工厂 ───────────────────────────────────────

    /** 两个相同 name 的 key 视为相等。 */
    static ChatStepKey of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new SimpleChatStepKey(name);
    }

    /**
     * 默认实现，基于 name 的 record。
     */
    record SimpleChatStepKey(String name) implements ChatStepKey {
    }
}