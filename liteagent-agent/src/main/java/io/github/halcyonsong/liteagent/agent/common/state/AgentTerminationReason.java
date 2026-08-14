package io.github.halcyonsong.liteagent.agent.common.state;

/**
 * 单次 ChatAgent 调用的终止原因。
 */
public enum AgentTerminationReason {
    COMPLETED,              // 正常完成
    MAX_ITERATIONS_REACHED, // 达到最大执行轮次
    TOOL_EXECUTION_FAILED,  // 工具执行失败
    MODEL_ERROR,            // 模型调用失败
    CANCELLED               // 被外部主动取消
}