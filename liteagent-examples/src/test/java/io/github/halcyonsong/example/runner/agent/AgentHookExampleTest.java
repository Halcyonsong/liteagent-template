package io.github.halcyonsong.example.runner.agent;

import io.github.halcyonsong.example.config.OpenAiConfig;
import io.github.halcyonsong.example.support.OpenAiExampleSupport;
import io.github.halcyonsong.liteagent.agent.chat.hook.StepHook;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
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

@SpringBootTest(classes = OpenAiConfig.class)
class AgentHookExampleTest extends OpenAiExampleSupport {

    @Test
    void chat_agent_hooks_should_fire_before_and_after_each_step() {
        assumeConfigReady();

        List<String> traces = new ArrayList<>();

        StepHook tracingHook = new StepHook() {
            @Override
            public void beforeStep(io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey key,
                                   io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext context) {
                traces.add("before:" + key.name());
            }

            @Override
            public void afterStep(io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey key,
                                  io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext context,
                                  io.github.halcyonsong.liteagent.agent.chat.step.ChatStepKey nextKey) {
                traces.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        OpenAiChatAgent agent = OpenAiChatAgents.create(
                buildRuntimeConfig(),
                List.of(tracingHook),
                100
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        agent.execute(OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
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
            public void beforeStep(io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey key,
                                   io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext<?> context) {
                traces.add("before:" + key.name());
            }

            @Override
            public void afterStep(io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey key,
                                  io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext<?> context,
                                  io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey nextKey) {
                traces.add("after:" + key.name() + "->" + nextKey.name());
            }
        };

        OpenAiStreamAgent agent = OpenAiStreamAgents.create(
                buildStreamRuntimeConfig(),
                List.of(tracingHook),
                100
        );

        ChatRequest chatRequest = ChatRequest.builder()
                .addMessage(Messages.user("你好"))
                .build();

        agent.execute(OpenAiChatCompletionRequest.builder()
                .baseRequest(createBaseRequest())
                .chatRequest(chatRequest)
                .build())
                .blockLast();

        System.out.println("===== Stream Agent Hook Traces =====");
        traces.forEach(System.out::println);
    }
}
