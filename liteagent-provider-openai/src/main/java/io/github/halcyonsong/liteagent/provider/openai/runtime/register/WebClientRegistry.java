package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeKey;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础 WebClient 注册表。
 * <p>
 * 按运行时配置缓存和复用 WebClient 实例，避免重复构建底层 HTTP 客户端。
 */
public class WebClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(WebClientRegistry.class);

    private final WebClientFactory factory;
    private final Map<HttpRuntimeKey, WebClient> clientCache = new ConcurrentHashMap<>();

    public WebClientRegistry(WebClientFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
    }

    public WebClient getOrCreateChatClient(HttpRuntimeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.CHAT);

        WebClient existing = clientCache.get(key);
        if (existing != null) {
            log.debug("Reusing cached chat WebClient. cacheSize={}, runtimeKey={}", clientCache.size(), key);
            return existing;
        }

        log.debug("Creating new chat WebClient. cacheSizeBefore={}, runtimeKey={}", clientCache.size(), key);

        return clientCache.computeIfAbsent(key, ignored -> factory.createChatClient(config));
    }

    public WebClient getOrCreateStreamClient(HttpRuntimeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.STREAM);

        WebClient existing = clientCache.get(key);
        if (existing != null) {
            log.debug("Reusing cached stream WebClient. cacheSize={}, runtimeKey={}", clientCache.size(), key);
            return existing;
        }

        log.debug("Creating new stream WebClient. cacheSizeBefore={}, runtimeKey={}", clientCache.size(), key);

        return clientCache.computeIfAbsent(key, ignored -> factory.createStreamClient(config));
    }

    public WebClient get(HttpRuntimeKey key) {
        Objects.requireNonNull(key, "key must not be null");
        WebClient client = clientCache.get(key);
        log.debug("Get WebClient by key. found={}, runtimeKey={}", client != null, key);
        return client;
    }

    public void remove(HttpRuntimeConfig config, HttpRuntimeMode mode) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(mode, "mode must not be null");

        HttpRuntimeKey key = config.toKey(mode);
        clientCache.remove(key);

        log.debug("Removed WebClient from registry. cacheSizeNow={}, runtimeKey={}",
                clientCache.size(),
                key);
    }

    public void clear() {
        int sizeBefore = clientCache.size();
        clientCache.clear();
        log.debug("Cleared WebClient registry. cacheSizeBefore={}, cacheSizeNow={}", sizeBefore, clientCache.size());
    }

    public int size() {
        return clientCache.size();
    }
}