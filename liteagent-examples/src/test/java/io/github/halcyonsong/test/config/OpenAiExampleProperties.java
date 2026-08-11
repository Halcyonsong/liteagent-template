package io.github.halcyonsong.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "liteagent.examples.openai")
public class OpenAiExampleProperties {

    private Boolean enabled = true;
    private String baseUrl;
    private String apiKey;
    private String model;
    private Runtime runtime = new Runtime();

    @Data
    public static class Runtime {
        private Integer maxInMemorySize = 16 * 1024 * 1024;
        private Integer connectTimeoutMillis = 5000;
        private Long responseTimeoutMillis = 60000L;
    }
}