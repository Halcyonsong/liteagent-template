package io.github.halcyonsong.example.runner.memory;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.example.support.Printers;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 演示 Chat Agent + 记忆窗口：同一 sessionId 的连续两轮对话，第二轮能记住第一轮的用户名。
 *
 * <p>Agent 通过 {@code chatAgentWithMemory} 注入，记忆 Hook 在 END 前自动折叠并写回窗口，
 * INIT_WORKING_MESSAGES 后自动读取历史拼到 workingMessages 前面。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class MemoryChatExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_with_memory_should_remember_previous_round() {
        assumeConfigReady();

        String sessionId = "demo-chat-" + System.currentTimeMillis();

        // ── 第一轮：告诉模型用户名 ──────────────────────────────
        ChatRequest firstRequest = ChatRequest.builder()
                .sessionId(sessionId)
                .addMessage(Messages.system("你是一位助手。"))
                .addMessage(Messages.user("我叫小明，请记住我的名字。"))
                .build();

        OpenAiChatCompletionRequest request1 = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(firstRequest)
                .build();

        System.out.println("===== Memory Chat Round 1 =====");
        OpenAiChatCompletionResponse response1 = chatAgentWithMemory.execute(request1);
        Printers.printChatResponse(response1);

        // ── 第二轮：验证模型记住用户名 ──────────────────────────
        ChatRequest secondRequest = ChatRequest.builder()
                .sessionId(sessionId)
                .addMessage(Messages.user("我刚才告诉你我叫什么名字？"))
                .build();

        OpenAiChatCompletionRequest request2 = OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(secondRequest)
                .build();

        System.out.println("===== Memory Chat Round 2 =====");
        OpenAiChatCompletionResponse response2 = chatAgentWithMemory.execute(request2);
        Printers.printChatResponse(response2);
    }
}
