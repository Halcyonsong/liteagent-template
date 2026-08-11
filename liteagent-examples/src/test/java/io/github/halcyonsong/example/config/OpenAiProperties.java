package io.github.halcyonsong.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "liteagent.openai")
public class OpenAiProperties {

    /**
     * 是否启用示例运行。
     */
    private Boolean enabled = true;

    /**
     * 启动后执行哪种示例：
     * invocation / provider / quick / all
     */
    private String mode = "all";

    private String baseUrl;
    private String apiKey;
    private String model;

    private Runtime runtime = new Runtime();

    @Data
    public static class Runtime {
        private Integer maxInMemorySize = 16 * 1024 * 1024;
        private Integer connectTimeoutMillis = 5000;
        private Long responseTimeoutMillis = 60000L;
        private Long streamResponseTimeoutMillis;
    }
}