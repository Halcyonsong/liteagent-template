package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.agent.stream.StreamAgent;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.agent.stream.hook.StreamStepHook;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStep;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamStepKey;
import io.github.halcyonsong.liteagent.agent.stream.step.StreamSyncStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamBeginStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamEnhanceRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamInitWorkingMessagesStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamMapRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.request.OpenAiStreamSendRequestStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamAccumulateChunkStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamAnalyzeChunkStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamBuildResultStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamDecideNextActionStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamEnhanceChunkStep;
import io.github.halcyonsong.liteagent.provider.openai.agent.stream.step.response.OpenAiStreamExecuteToolStep;
import io.github.halcyonsong.liteagent.provider.openai.client.support.OpenAiClientSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatStreamTransport;
import reactor.core.publisher.Flux;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAI-compatible 流式执行器装配工厂。
 */
public class OpenAiStreamAgentExecutorFactory {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiClientSupport clientSupport;
    private final OpenAiChatStreamTransport streamTransport;
    private final OpenAiStreamResponseMapper responseMapper;

    public OpenAiStreamAgentExecutorFactory(OpenAiChatRequestMapper requestMapper,
                                            OpenAiClientSupport clientSupport,
                                            OpenAiChatStreamTransport streamTransport,
                                            OpenAiStreamResponseMapper responseMapper) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.clientSupport = Objects.requireNonNull(clientSupport, "clientSupport must not be null");
        this.streamTransport = Objects.requireNonNull(streamTransport, "streamTransport must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
    }

    public StreamAgentExecutor<OpenAiStreamCompletionResponse> create() {
        return create(List.of(), 1000);
    }

    public StreamAgentExecutor<OpenAiStreamCompletionResponse> create(List<StreamStepHook> hooks,
                                                                      int maxStepCount) {
        Map<StreamStepKey, StreamSyncStep> syncSteps = new EnumMap<>(StreamStepKey.class);
        syncSteps.put(StreamStepKey.BEGIN, new OpenAiStreamBeginStep());
        syncSteps.put(StreamStepKey.INIT_WORKING_MESSAGES, new OpenAiStreamInitWorkingMessagesStep());
        syncSteps.put(StreamStepKey.MAP_REQUEST, new OpenAiStreamMapRequestStep(requestMapper));
        syncSteps.put(StreamStepKey.ENHANCE_REQUEST, new OpenAiStreamEnhanceRequestStep(clientSupport));
        syncSteps.put(StreamStepKey.DECIDE_NEXT_ACTION, new OpenAiStreamDecideNextActionStep());
        syncSteps.put(StreamStepKey.EXECUTE_TOOL, new OpenAiStreamExecuteToolStep());
        syncSteps.put(StreamStepKey.BUILD_RESULT, new OpenAiStreamBuildResultStep());
        syncSteps.put(StreamStepKey.END, context -> StreamStepKey.END);

        Map<StreamStepKey, StreamStep<Flux<OpenAiStreamCompletionResponse>>> streamSteps =
                new EnumMap<>(StreamStepKey.class);
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