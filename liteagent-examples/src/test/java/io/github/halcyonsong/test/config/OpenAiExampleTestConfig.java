package io.github.halcyonsong.test.config;

import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClient;
import io.github.halcyonsong.liteagent.provider.openai.client.OpenAiChatClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.config.HttpRuntimeConfig;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientFactory;
import io.github.halcyonsong.liteagent.provider.openai.runtime.register.WebClientRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAiExampleProperties.class)
public class OpenAiExampleTestConfig {

    @Bean
    public HttpRuntimeConfig httpRuntimeConfig(OpenAiExampleProperties properties) {
        return HttpRuntimeConfig.builder()
                .maxInMemorySize(properties.getRuntime().getMaxInMemorySize())
                .connectTimeoutMillis(properties.getRuntime().getConnectTimeoutMillis())
                .responseTimeoutMillis(properties.getRuntime().getResponseTimeoutMillis())
                .build();
    }

    @Bean
    public WebClientRegistry webClientRegistry() {
        return new WebClientRegistry(new WebClientFactory());
    }

    @Bean
    public OpenAiChatClientFactory openAiChatClientFactory(WebClientRegistry webClientRegistry) {
        return new OpenAiChatClientFactory(webClientRegistry);
    }

    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiChatClientFactory factory,
                                             HttpRuntimeConfig runtimeConfig) {
        return factory.create(runtimeConfig);
    }
}