package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeKey;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础 WebClient 注册表。按运行时配置缓存和复用 WebClient 实例。
 */
@Slf4j
public class WebClientRegistry {

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
            log.debug("Reusing cached chat WebClient. cacheSize={}", clientCache.size());
            return existing;
        }

        log.debug("Creating new chat WebClient. cacheSize={}", clientCache.size());

        return clientCache.computeIfAbsent(key, ignored -> factory.createChatClient(config));
    }

    public WebClient getOrCreateStreamClient(HttpRuntimeConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        HttpRuntimeKey key = config.toKey(HttpRuntimeMode.STREAM);

        WebClient existing = clientCache.get(key);
        if (existing != null) {
            log.debug("Reusing cached stream WebClient. cacheSize={}", clientCache.size());
            return existing;
        }

        log.debug("Creating new stream WebClient. cacheSize={}", clientCache.size());

        return clientCache.computeIfAbsent(key, ignored -> factory.createStreamClient(config));
    }

    public WebClient get(HttpRuntimeKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return clientCache.get(key);
    }

    public void remove(HttpRuntimeConfig config, HttpRuntimeMode mode) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(mode, "mode must not be null");

        HttpRuntimeKey key = config.toKey(mode);
        clientCache.remove(key);

        log.debug("Removed WebClient. cacheSize={}", clientCache.size());
    }

    public void clear() {
        int sizeBefore = clientCache.size();
        clientCache.clear();
        log.debug("Cleared registry. sizeBefore={}, sizeNow={}", sizeBefore, clientCache.size());
    }

    public int size() {
        return clientCache.size();
    }
}