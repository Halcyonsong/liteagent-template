package io.github.halcyonsong.liteagent.provider.openai.runtime.register;

import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * 基础 WebClient 工厂。
 * <p>
 * 仅负责创建运行时客户端，不负责 provider 认证和 endpoint 绑定。
 */
@Slf4j
public class WebClientFactory {

    public WebClient createChatClient(HttpRuntimeConfig config) {
        log.debug("Creating chat WebClient. maxInMemorySize={}, connectTimeoutMillis={}, responseTimeoutMillis={}",
                config.getMaxInMemorySize(),
                config.getConnectTimeoutMillis(),
                config.getResponseTimeoutMillis());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(config.getResponseTimeoutMillis()));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(config.getMaxInMemorySize()))
                .build();
    }

    public WebClient createStreamClient(HttpRuntimeConfig config) {
        log.debug("Creating stream WebClient. maxInMemorySize={}, connectTimeoutMillis={}, streamResponseTimeoutMillis={}",
                config.getMaxInMemorySize(),
                config.getConnectTimeoutMillis(),
                config.getStreamResponseTimeoutMillis());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis());

        if (config.getStreamResponseTimeoutMillis() != null) {
            httpClient = httpClient.responseTimeout(Duration.ofMillis(config.getStreamResponseTimeoutMillis()));
        }

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(config.getMaxInMemorySize()))
                .build();
    }
}