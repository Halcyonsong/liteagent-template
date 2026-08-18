package io.github.halcyonsong.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "liteagent.openai")
public class OpenAiProperties {

    private Boolean enabled = true;

    /** 启动后执行哪种示例：chat / stream / tool / memory / hook / quick / all */
    private String mode = "all";

    private String baseUrl;
    private String apiKey;
    private String model;

    private Runtime runtime = new Runtime();
    private Memory memory = new Memory();

    @Data
    public static class Runtime {
        private Integer maxInMemorySize = 16 * 1024 * 1024;
        private Integer connectTimeoutMillis = 5000;
        private Long responseTimeoutMillis = 60000L;
        private Long streamResponseTimeoutMillis;
    }

    @Data
    public static class Memory {
        /** Chat 记忆窗口最大消息数，默认 40。 */
        private Integer chatMaxSize = 40;
        /** Stream 记忆窗口最大消息数，默认 100。 */
        private Integer streamMaxSize = 100;
    }
}
