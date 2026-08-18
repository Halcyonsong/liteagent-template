package io.github.halcyonsong.example.runner.memory;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 演示 Stream Agent + 记忆窗口：同一 sessionId 的连续两轮流式对话，第二轮能记住第一轮内容。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class MemoryStreamExampleTest extends OpenAiExampleSupport {

    @Test
    void stream_with_memory_should_remember_previous_round() {
        assumeConfigReady();

        String sessionId = "demo-stream-" + System.currentTimeMillis();

        // ── 第一轮 ─────────────────────────────────────────────
        ChatRequest firstRequest = ChatRequest.builder()
                .sessionId(sessionId)
                .addMessage(Messages.system("你是一位助手。"))
                .addMessage(Messages.user("我叫小红，请记住我的名字。"))
                .build();

        OpenAiChatCompletionRequest request1 = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(firstRequest)
                .build();

        System.out.println("===== Memory Stream Round 1 =====");
        streamAgentWithMemory.execute(request1)
                .doOnNext(Printers::printStreamDeltaAll)
                .blockLast();
        System.out.println("\n===== Stream Round 1 End =====");

        // ── 第二轮 ─────────────────────────────────────────────
        ChatRequest secondRequest = ChatRequest.builder()
                .sessionId(sessionId)
                .addMessage(Messages.user("我刚才告诉你我叫什么名字？"))
                .build();

        OpenAiChatCompletionRequest request2 = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(secondRequest)
                .build();

        System.out.println("===== Memory Stream Round 2 =====");
        streamAgentWithMemory.execute(request2)
                .doOnNext(Printers::printStreamDeltaAll)
                .blockLast();
        System.out.println("\n===== Stream Round 2 End =====");
    }
}
