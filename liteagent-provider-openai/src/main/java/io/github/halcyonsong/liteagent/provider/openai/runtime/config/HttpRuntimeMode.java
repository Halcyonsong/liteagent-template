package io.github.halcyonsong.liteagent.provider.openai.runtime.config;

/**
 * WebClient 运行时模式，区分普通聊天和流式请求的 HTTP 实例配置。
 */
public enum HttpRuntimeMode {

    /** 普通对话模式。 */
    CHAT,

    /** 流式对话模式。 */
    STREAM
}