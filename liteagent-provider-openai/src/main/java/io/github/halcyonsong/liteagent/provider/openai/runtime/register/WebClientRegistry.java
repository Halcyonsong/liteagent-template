package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeKey;
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

    public WebClient getOrCreate(HttpRuntimeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        HttpRuntimeKey key = config.toKey();

        WebClient existing = clientCache.get(key);
        if (existing != null) {
            log.debug("Reusing cached WebClient. cacheSize={}, runtimeKey={}", clientCache.size(), key);
            return existing;
        }

        log.debug("Creating new WebClient. cacheSizeBefore={}, runtimeKey={}", clientCache.size(), key);

        return clientCache.computeIfAbsent(key, ignored -> factory.create(config));
    }

    public WebClient get(HttpRuntimeKey key) {
        Objects.requireNonNull(key, "key must not be null");
        WebClient client = clientCache.get(key);
        log.debug("Get WebClient by key. found={}, runtimeKey={}", client != null, key);
        return client;
    }

    public void remove(HttpRuntimeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        clientCache.remove(config.toKey());
        log.debug("Removed WebClient from registry. cacheSizeNow={}, runtimeKey={}",
                clientCache.size(),
                config.toKey());
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