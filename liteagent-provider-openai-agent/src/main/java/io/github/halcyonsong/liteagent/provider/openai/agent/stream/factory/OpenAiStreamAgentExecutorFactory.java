package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.core.tool.impl.ReflectionToolExecutor;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolExecutor;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamInitToolRegistryStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamInitWorkingMessagesStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamSendRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.*;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiStreamTransport;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpenAiStreamAgentExecutorFactory {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiAdvisorsSupport clientSupport;
    private final OpenAiStreamTransport streamTransport;
    private final OpenAiStreamResponseMapper responseMapper;
    private final ToolExecutor toolExecutor;

    public OpenAiStreamAgentExecutorFactory(
            OpenAiChatRequestMapper requestMapper,
            OpenAiAdvisorsSupport clientSupport,
            OpenAiStreamTransport streamTransport,
            OpenAiStreamResponseMapper responseMapper
    ) {
        this(requestMapper, clientSupport, streamTransport, responseMapper, new ReflectionToolExecutor());
    }

    public OpenAiStreamAgentExecutorFactory(
            OpenAiChatRequestMapper requestMapper,
            OpenAiAdvisorsSupport clientSupport,
            OpenAiStreamTransport streamTransport,
            OpenAiStreamResponseMapper responseMapper,
            ToolExecutor toolExecutor
    ) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
        this.streamTransport = Objects.requireNonNull(streamTransport, "streamTransport must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
    }

    public StreamAgentExecutor<OpenAiStreamCompletionResponse> create() {
        return create(List.of(), 1000);
    }

    public StreamAgentExecutor<OpenAiStreamCompletionResponse> create(List<StreamStepHook> hooks,
                                                                      int maxStepCount) {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new HashMap<>();
        syncSteps.put(StreamStepKey.BEGIN, new OpenAiStreamBeginStep());
        syncSteps.put(StreamStepKey.INIT_WORKING_MESSAGES, new OpenAiStreamInitWorkingMessagesStep());
        syncSteps.put(StreamStepKey.INIT_TOOL_REGISTRY, new OpenAiStreamInitToolRegistryStep());
        syncSteps.put(StreamStepKey.MAP_REQUEST, new OpenAiStreamMapRequestStep(requestMapper));
        syncSteps.put(StreamStepKey.ENHANCE_REQUEST, new OpenAiStreamEnhanceRequestStep(clientSupport));
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, new OpenAiStreamDecideNextActionStep());
        syncSteps.put(StreamStepKey.EXECUTE_TOOL, new OpenAiStreamExecuteToolStep(toolExecutor));
        syncSteps.put(StreamStepKey.APPEND_MESSAGES, new OpenAiStreamAppendMessagesStep());
        syncSteps.put(StreamStepKey.BUILD_RESULT, new OpenAiStreamBuildResultStep());
        syncSteps.put(StreamStepKey.END, context -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<OpenAiStreamCompletionResponse>>> streamSteps = new HashMap<>();
        streamSteps.put(StreamStepKey.SEND_REQUEST, new OpenAiStreamSendRequestStep(streamTransport, responseMapper));
        streamSteps.put(StreamStepKey.ENHANCE_CHUNK, new OpenAiStreamEnhanceChunkStep(clientSupport));
        streamSteps.put(StreamStepKey.ACCUMULATE_CHUNK, new OpenAiStreamAccumulateChunkStep());
        streamSteps.put(StreamStepKey.ANALYZE_CHUNK, new OpenAiStreamAnalyzeChunkStep());

        return new StreamAgentExecutor<>(syncSteps, streamSteps, hooks, maxStepCount);
    }

    public StreamAgent<OpenAiStreamCompletionResponse> createAgent() {
        return new StreamAgent<>(create());
    }

    public StreamAgent<OpenAiStreamCompletionResponse> createAgent(List<StreamStepHook> hooks,
                                                                   int maxStepCount) {
        return new StreamAgent<>(create(hooks, maxStepCount));
    }
}