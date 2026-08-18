package io.github.halcyonsong.liteagent.agent.state;

/**
 * 单次 ChatAgent 调用的终止原因。
 */
public enum AgentTerminationReason {
    COMPLETED,
    MAX_ITERATIONS_REACHED,
    TOOL_ERROR,
    MODEL_ERROR,
    CANCELLED
}