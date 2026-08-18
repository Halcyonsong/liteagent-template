package io.github.halcyonsong.liteagent.core.exception;

/**
 * LiteAgent 运行期间的错误分类。
 */
public enum ErrorCode {

    /**
     * 未分类错误。
     */
    UNKNOWN,

    /**
     * 请求模型时，请求参数、URL 或请求格式错误。
     */
    MODEL_REQUEST_ERROR,

    /**
     * 模型返回内容为空、格式错误或无法解析。
     */
    MODEL_RESPONSE_ERROR,

    /**
     * 模型服务网络连接失败。
     */
    MODEL_NETWORK_ERROR,

    /**
     * 模型请求或响应超时。
     */
    MODEL_TIMEOUT_ERROR,

    /**
     * 模型服务限流。
     */
    MODEL_RATE_LIMIT_ERROR,

    /**
     * API Key、权限或身份认证失败。
     */
    MODEL_AUTH_ERROR,

    /**
     * 当前模型不支持请求的能力，例如 tool calls。
     */
    MODEL_CAPABILITY_ERROR,

    /**
     * 当前账户、套餐或策略不允许使用该模型或能力。
     */
    MODEL_POLICY_ERROR,

    /**
     * 模型不存在、模型没有可用渠道或路由失败。
     */
    MODEL_ROUTING_ERROR,

    /**
     * 模型服务端发生错误。
     */
    MODEL_SERVER_ERROR,

    /**
     * 工具执行失败。
     */
    TOOL_EXECUTION_ERROR
}