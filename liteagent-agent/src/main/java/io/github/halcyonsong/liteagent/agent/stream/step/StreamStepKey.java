package io.github.halcyonsong.liteagent.agent.stream.step;

/**
 * 流式编排步骤标识。内置常量覆盖标准流式编排流程，也支持通过 {@link #of(String)} 创建自定义 key。
 */
public interface StreamStepKey {

    /**
     * 步骤标识名称，用于日志、调试和相等性判断。
     */
    String name();

    // ─── 内置步骤 ───────────────────────────────────────────────

    StreamStepKey BEGIN = of("BEGIN");
    StreamStepKey INIT_WORKING_MESSAGES = of("INIT_WORKING_MESSAGES");
    StreamStepKey INIT_TOOL_REGISTRY = of("INIT_TOOL_REGISTRY");
    StreamStepKey MAP_REQUEST = of("MAP_REQUEST");
    StreamStepKey ENHANCE_REQUEST = of("ENHANCE_REQUEST");
    StreamStepKey SEND_REQUEST = of("SEND_REQUEST");
    StreamStepKey ENHANCE_CHUNK = of("ENHANCE_CHUNK");
    StreamStepKey ACCUMULATE_CHUNK = of("ACCUMULATE_CHUNK");
    StreamStepKey ANALYZE_CHUNK = of("ANALYZE_CHUNK");
    StreamStepKey DECIDE_NEXT_ACTION = of("DECIDE_NEXT_ACTION");
    StreamStepKey STREAM_END = of("STREAM_END");
    StreamStepKey EXECUTE_TOOL = of("EXECUTE_TOOL");
    StreamStepKey APPEND_MESSAGES = of("APPEND_MESSAGES");
    StreamStepKey BUILD_RESULT = of("BUILD_RESULT");
    StreamStepKey END = of("END");

    // ─── 自定义 key 工厂 ───────────────────────────────────────

    /** 两个相同 name 的 key 视为相等。 */
    static StreamStepKey of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new SimpleStreamStepKey(name);
    }

    /**
     * 默认实现，基于 name 的 record。
     */
    record SimpleStreamStepKey(String name) implements StreamStepKey {
    }
}
