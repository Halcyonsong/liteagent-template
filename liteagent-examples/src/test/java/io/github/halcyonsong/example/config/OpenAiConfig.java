package io.github.halcyonsong.example.config;

import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiStreamClient;
import io.github.halcyonsong.liteagent.provider.openai.client.factory.OpenAiClients;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    private final OpenAiProperties openAiProperties;

    @Bean
    public OpenAiChatClient openAiChatClient() {
        return OpenAiClients.create(
                openAiProperties.getRuntime().getMaxInMemorySize(),
                openAiProperties.getRuntime().getConnectTimeoutMillis(),
                openAiProperties.getRuntime().getResponseTimeoutMillis()
        );
    }

    @Bean
    public OpenAiStreamClient openAiStreamClient() {
        return OpenAiClients.createStream(
                openAiProperties.getRuntime().getMaxInMemorySize(),
                openAiProperties.getRuntime().getConnectTimeoutMillis(),
                openAiProperties.getRuntime().getStreamResponseTimeoutMillis()
        );
    }
}