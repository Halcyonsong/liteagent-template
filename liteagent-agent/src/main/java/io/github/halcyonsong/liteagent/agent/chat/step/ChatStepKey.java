package io.github.halcyonsong.liteagent.agent.chat.step;

/**
 * 同步 chat 编排内部使用的步骤标识。
 */
public enum ChatStepKey {
    BEGIN,                  // 起始步骤
    INIT_WORKING_MESSAGES,  // 初始化工作态消息历史
    INIT_TOOL_REGISTRY,     // 初始化工具注册表
    MAP_REQUEST,            // 将统一输入映射为 provider 请求
    ENHANCE_REQUEST,        // 对请求应用 advisor / 增强逻辑
    SEND_REQUEST,           // 发送普通对话请求
    MAP_RESPONSE,           // 映射普通对话响应
    ENHANCE_RESPONSE,       // 对响应应用 advisor / 增强逻辑
    ANALYZE_RESPONSE,       // 分析响应，决定下一步
    EXECUTE_TOOL,           // 执行工具并准备下一轮
    APPEND_MESSAGES,        // 追加暂存的工具执行结果史
    BUILD_RESULT,           // 构建最终结果
    END                     // 结束
}