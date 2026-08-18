package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * 基础 WebClient 工厂。
 * <p>
 * 仅负责创建运行时客户端，不负责 provider 认证和 endpoint 绑定。
 */
@Slf4j
public class WebClientFactory {

    public WebClient createChatClient(HttpRuntimeConfig config) {
        log.debug("Creating chat WebClient. maxConnections={}, connectTimeoutMillis={}, responseTimeoutMillis={}",
                config.getMaxConnections(),
                config.getConnectTimeoutMillis(),
                config.getChatResponseTimeoutMillis());

        HttpClient httpClient = HttpClient.create(buildConnectionProvider(config))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(config.getChatResponseTimeoutMillis()));

        return buildWebClient(httpClient, config);
    }

    public WebClient createStreamClient(HttpRuntimeConfig config) {
        log.debug("Creating stream WebClient. maxConnections={}, connectTimeoutMillis={}, streamResponseTimeoutMillis={}",
                config.getMaxConnections(),
                config.getConnectTimeoutMillis(),
                config.getStreamResponseTimeoutMillis());

        HttpClient httpClient = HttpClient.create(buildConnectionProvider(config))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis());

        if (config.getStreamResponseTimeoutMillis() != null) {
            httpClient = httpClient.responseTimeout(Duration.ofMillis(config.getStreamResponseTimeoutMillis()));
        }

        return buildWebClient(httpClient, config);
    }

    private ConnectionProvider buildConnectionProvider(HttpRuntimeConfig config) {
        String name = "liteagent-pool-" + Integer.toHexString(config.hashCode());
        return ConnectionProvider.builder(name)
                .maxConnections(config.getMaxConnections())
                .maxIdleTime(Duration.ofMillis(config.getMaxIdleTimeMillis()))
                .maxLifeTime(Duration.ofMillis(config.getMaxLifeTimeMillis()))
                .pendingAcquireTimeout(Duration.ofSeconds(10))   // 池耗尽时最多等 10s
                .evictInBackground(Duration.ofSeconds(20))         // 每 20s 后台清理一次
                .build();
    }

    private WebClient buildWebClient(HttpClient httpClient, HttpRuntimeConfig config) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(config.getMaxInMemorySize()))
                .build();
    }
}