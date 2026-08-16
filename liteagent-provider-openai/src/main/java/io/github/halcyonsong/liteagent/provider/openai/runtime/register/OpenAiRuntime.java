// runtime/OpenAiRuntime.java
package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 全局共享的 WebClient 运行时。
 * <p>
 * 持有唯一的 WebClientRegistry 实例，chat 和 stream 共享同一份缓存。
 * 非 Spring 场景直接使用静态方法；Spring 场景可忽略此类，自行注入 WebClientRegistry Bean。
 */
public final class OpenAiRuntime {

    private static final WebClientFactory WEB_CLIENT_FACTORY = new WebClientFactory();

    private static final WebClientRegistry WEB_CLIENT_REGISTRY = new WebClientRegistry(WEB_CLIENT_FACTORY);

    private OpenAiRuntime() {
    }

    public static WebClientRegistry sharedRegistry() {
        return WEB_CLIENT_REGISTRY;
    }

    public static WebClient getOrCreateChatClient(HttpRuntimeConfig config) {
        return WEB_CLIENT_REGISTRY.getOrCreateChatClient(config);
    }

    public static WebClient getOrCreateStreamClient(HttpRuntimeConfig config) {
        return WEB_CLIENT_REGISTRY.getOrCreateStreamClient(config);
    }
}