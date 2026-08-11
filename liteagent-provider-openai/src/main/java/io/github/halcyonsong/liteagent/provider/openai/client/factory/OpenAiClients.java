package io.github.halcyonsong.liteagent.provider.openai.client.factory;

import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;

/**
 * OpenAI-compatible 客户端快速创建入口。
 * <p>
 * 该工具类用于隐藏运行时配置、WebClient 注册表以及客户端工厂等底层装配细节，
 * 让普通 Java 用户可以用最少代码直接创建 {@link OpenAiChatClient}。
 * <p>
 * 典型用法：
 * <pre>{@code
 * OpenAiChatClient client = OpenAiClients.createDefault();
 * }</pre>
 */
public final class OpenAiClients {

    private OpenAiClients() {
    }

    /**
     * 使用默认运行时配置创建 OpenAI-compatible 客户端。
     * <p>
     * 当前默认值通常包括：
     * <ul>
     *   <li>maxInMemorySize = 16MB</li>
     *   <li>connectTimeoutMillis = 5000</li>
     *   <li>responseTimeoutMillis = 60000</li>
     * </ul>
     * <p>
     * 适合快速测试、示例场景以及对运行时参数没有特殊要求的普通调用。
     *
     * @return 已完成基础装配的 {@link OpenAiChatClient}
     */
    public static OpenAiChatClient createDefault() {
        HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder().build();
        return create(runtimeConfig);
    }

    /**
     * 使用指定运行时配置创建 OpenAI-compatible 客户端。
     * <p>
     * 适合需要显式控制底层 HTTP 运行时参数的场景，例如：
     * <ul>
     *   <li>调整最大响应缓冲大小</li>
     *   <li>调整连接超时</li>
     *   <li>调整响应超时</li>
     * </ul>
     *
     * @param runtimeConfig 底层 HTTP 客户端运行时配置
     * @return 已完成基础装配的 {@link OpenAiChatClient}
     */
    public static OpenAiChatClient create(HttpRuntimeConfig runtimeConfig) {
        WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());
        OpenAiChatClientFactory factory = new OpenAiChatClientFactory(registry);
        return factory.create(runtimeConfig);
    }

    /**
     * 使用最常见的三个运行时参数快速创建 OpenAI-compatible 客户端。
     * <p>
     * 该方法是 {@link #create(HttpRuntimeConfig)} 的便捷重载，
     * 适合不希望手动构造 {@link HttpRuntimeConfig} 的调用方。
     *
     * @param maxInMemorySize       响应体最大内存缓冲大小，单位字节
     * @param connectTimeoutMillis  连接超时时间，单位毫秒
     * @param responseTimeoutMillis 响应超时时间，单位毫秒
     * @return 已完成基础装配的 {@link OpenAiChatClient}
     */
    public static OpenAiChatClient create(Integer maxInMemorySize,
                                          Integer connectTimeoutMillis,
                                          Long responseTimeoutMillis) {
        HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder()
                .maxInMemorySize(maxInMemorySize)
                .connectTimeoutMillis(connectTimeoutMillis)
                .responseTimeoutMillis(responseTimeoutMillis)
                .build();
        return create(runtimeConfig);
    }

    /**
     * 使用默认运行时配置创建 OpenAI-compatible 流式客户端。
     * <p>
     * 当前默认值通常包括：
     * <ul>
     *   <li>maxInMemorySize = 16MB</li>
     *   <li>connectTimeoutMillis = 5000</li>
     *   <li>responseTimeoutMillis = 60000</li>
     * </ul>
     * <p>
     * 适合快速测试、示例场景以及对流式运行时参数没有特殊要求的普通调用。
     *
     * @return 已完成基础装配的 {@link io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient}
     */
    public static OpenAiStreamClient createStreamDefault() {
        HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder().build();
        return createStream(runtimeConfig);
    }

    /**
     * 使用指定运行时配置创建 OpenAI-compatible 流式客户端。
     * <p>
     * 适合需要显式控制底层 HTTP 运行时参数的流式场景，例如：
     * <ul>
     *   <li>调整最大响应缓冲大小</li>
     *   <li>调整连接超时</li>
     *   <li>调整响应超时</li>
     * </ul>
     * <p>
     * 该方法创建的客户端用于发送流式对话请求，
     * 返回基于 Flux 的流式响应结果。
     *
     * @param runtimeConfig 底层 HTTP 客户端运行时配置
     * @return 已完成基础装配的 {@link io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient}
     */
    public static OpenAiStreamClient createStream(HttpRuntimeConfig runtimeConfig) {
        WebClientRegistry registry = new WebClientRegistry(new WebClientFactory());
        OpenAiChatClientFactory factory = new OpenAiChatClientFactory(registry);
        return factory.createStream(runtimeConfig);
    }

    /**
     * 使用最常见的三个运行时参数快速创建 OpenAI-compatible 流式客户端。
     * <p>
     * 该方法是 {@link #createStream(io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig)}
     * 的便捷重载，适合不希望手动构造运行时配置对象的调用方。
     *
     * @param maxInMemorySize       响应体最大内存缓冲大小，单位字节
     * @param connectTimeoutMillis  连接超时时间，单位毫秒
     * @param streamResponseTimeoutMillis 流式响应超时时间，单位毫秒
     * @return 已完成基础装配的 {@link io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient}
     */
    public static OpenAiStreamClient createStream(Integer maxInMemorySize,
                                                  Integer connectTimeoutMillis,
                                                  Long streamResponseTimeoutMillis) {
        HttpRuntimeConfig runtimeConfig = HttpRuntimeConfig.builder()
                .maxInMemorySize(maxInMemorySize)
                .connectTimeoutMillis(connectTimeoutMillis)
                .streamResponseTimeoutMillis(streamResponseTimeoutMillis)
                .build();
        return createStream(runtimeConfig);
    }
}