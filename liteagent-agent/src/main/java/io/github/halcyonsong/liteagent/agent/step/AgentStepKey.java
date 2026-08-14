package io.github.halcyonsong.liteagent.agent.step;

/**
 * Agent 编排层内置步骤标识。
 */
public enum AgentStepKey {
    BEGIN,               // 起始步骤
    MAP_REQUEST,         // 将统一输入映射为 provider 请求
    ENHANCE_REQUEST,     // 对请求应用 advisor / 增强逻辑
    SEND_CHAT_REQUEST,   // 发送普通对话请求
    MAP_CHAT_RESPONSE,   // 映射普通对话响应
    SEND_STREAM_REQUEST, // 发送流式对话请求
    MAP_STREAM_RESPONSE, // 映射流式对话响应
    ENHANCE_RESPONSE,    // 对响应应用 advisor / 增强逻辑
    ANALYZE_RESPONSE,    // 分析响应，决定下一步
    EXECUTE_TOOL,        // 执行工具并准备下一轮
    BUILD_RESULT,        // 构建最终结果
    END                  // 结束
}