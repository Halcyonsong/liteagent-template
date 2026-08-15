package io.github.halcyonsong.liteagent.agent.stream.state;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 单轮流式请求的运行时状态。
 * <p>
 * 该对象描述某一轮流式请求的控制状态、增量聚合容器、
 * 最终聚合结果以及当前轮附加数据。
 * <p>
 * 它不承担整次编排的全局状态管理；
 * 多轮历史由 StreamAgentContext 中的 rounds 列表统一维护。
 */
@Getter
@Setter
public class StreamRoundState {

    /**
     * 当前轮索引，从 0 开始。
     */
    private final int roundIndex;

    /**
     * 当前轮是否已经完成。
     */
    private volatile boolean roundComplete;

    /**
     * provider-specific 的本轮增量聚合器。
     * <p>
     * 该对象通常用于在 chunk 到达过程中累积 content、reasoning、
     * tool calls、usage 等增量数据。
     */
    private Object accumulator;

    /**
     * provider-specific 的本轮最终聚合结果。
     * <p>
     * 该对象通常由 accumulator 在本轮结束后构造，
     * 可供 BUILD_RESULT、EXECUTE_TOOL 或调试逻辑读取。
     */
    private Object finalResponse;

    /**
     * 当前轮附加状态。
     * <p>
     * 可用于存放 toolCalls、finishReason、usage、raw response metadata
     * 等当前轮特有的 provider-specific 数据。
     */
    private final Map<String, Object> attributes = new HashMap<>();

    public StreamRoundState(int roundIndex) {
        if (roundIndex < 0) {
            throw new IllegalArgumentException("roundIndex must not be negative");
        }
        this.roundIndex = roundIndex;
    }

    /**
     * 写入或覆盖当前轮附加属性。
     */
    public void setAttribute(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("attribute key must not be blank");
        }
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * 读取当前轮附加属性。
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 以强类型方式读取当前轮附加属性。
     */
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }
}