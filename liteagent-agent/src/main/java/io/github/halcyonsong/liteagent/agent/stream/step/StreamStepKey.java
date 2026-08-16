package io.github.halcyonsong.liteagent.agent.stream.step;

/**
 * 流式编排步骤标识。
 * <p>
 * 以接口形式定义，内置常量覆盖标准流式编排流程，
 * 同时允许调用方通过 {@link #of(String)} 创建自定义 key，
 * 将自定义步骤插入链路。
 * <p>
 * 内置常量均为单例，建议使用 {@code equals} 比较；
 * 自定义 key 基于 name 做相等性判断。
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

    /**
     * 创建自定义步骤标识。
     * <p>
     * 两个相同 name 的 key 视为相等，可用于在步骤注册表中查找。
     *
     * @param name 步骤标识名称，不可为 blank
     * @return 新的步骤标识实例
     */
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
