package io.github.halcyonsong.liteagent.agent.stream.step;

/**
 * 流式编排内部使用的步骤标识。
 */
public enum StreamStepKey {
    BEGIN,                  // 起始步骤
    INIT_WORKING_MESSAGES,  // 初始化工作态消息
    INIT_TOOL_REGISTRY,     // 初始化工具注册表
    MAP_REQUEST,            // 将工作态消息映射为 provider 请求
    ENHANCE_REQUEST,        // 对请求应用 advisor / 增强逻辑
    SEND_REQUEST,           // 发起流式请求并创建源流
    ENHANCE_CHUNK,          // 对每个 chunk 应用增强逻辑
    ACCUMULATE_CHUNK,       // 累积 chunk 中间状态
    ANALYZE_CHUNK,          // 分析 chunk / 轮次完成信号
    DECIDE_NEXT_ACTION,     // 决策下一步：结束、构建结果或执行工具
    STREAM_END,             // 单轮流式管道构建结束
    EXECUTE_TOOL,           // 执行工具并准备下一轮
    APPEND_MESSAGES,        // 合并工具输出到工作态消息
    BUILD_RESULT,           // 构建最终结果
    END                     // 整体结束
}