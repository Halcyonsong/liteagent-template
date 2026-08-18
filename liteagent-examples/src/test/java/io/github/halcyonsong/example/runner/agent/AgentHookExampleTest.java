package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey;
import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.OpenAiChatAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory.OpenAiChatAgents;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.OpenAiStreamAgent;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory.OpenAiStreamAgents;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * StepHook / StreamStepHook 示例：注入 runtime config，用静态工厂创建带自定义 hook 的 Agent。
 *
 * <p>Hook 在每步执行前后触发，适合日志追踪、metrics 上报等横切逻辑。
 */
@SpringBootTest(classes = OpenAiConfig.class)
class AgentHookExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_agent_hooks_should_fire_before_and_after_each_step() {
        assumeConfigReady();

        List<String> traces = new ArrayList<>();

        StepHook tracingHook = new StepHook() {
            @Override
            public void beforeStep(ChatStepKey key, ChatAgentContext context) {
                traces.add("before:" + key.name());
            }

            @Override
            public void afterStep(ChatStepKey key, ChatAgentContext context, ChatStepKey nextKey) {
                traces.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        OpenAiChatAgent agent = OpenAiChatAgents.create(
                chatRuntimeConfig,
                List.of(tracingHook),
                100, 10
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        agent.execute(OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build());

        System.out.println("===== Chat Agent Hook Traces =====");
        traces.forEach(System.out::println);
    }

    @Test
    void stream_agent_hooks_should_fire_for_sync_steps() {
        assumeConfigReady();

        List<String> traces = new ArrayList<>();

        StreamStepHook tracingHook = new StreamStepHook() {
            @Override
            public void beforeStep(StreamStepKey key, StreamAgentContext<?> context) {
                traces.add("before:" + key.name());
            }

            @Override
            public void afterStep(StreamStepKey key, StreamAgentContext<?> context, StreamStepKey nextKey) {
                traces.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        OpenAiStreamAgent agent = OpenAiStreamAgents.create(
                streamRuntimeConfig,
                List.of(tracingHook),
                100, 10
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        agent.execute(OpenAiChatCompletionRequest.builder()
                .baseRequest(baseRequest)
                .chatRequest(chatRequest)
                .build())
                .blockLast();

        System.out.println("===== Stream Agent Hook Traces =====");
        traces.forEach(System.out::println);
    }
}
