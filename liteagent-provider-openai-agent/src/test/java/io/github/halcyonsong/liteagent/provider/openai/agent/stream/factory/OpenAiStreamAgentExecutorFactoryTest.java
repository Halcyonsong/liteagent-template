package io.github.halcyonsong.liteagent.provider.openai.agent.stream.factory;

import io.github.halcyonsong.liteagent.agent.stream.context.StreamAgentContext;
import io.github.halcyonsong.liteagent.agent.stream.executor.StreamAgentExecutor;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsSupport;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.config.stream.OpenAiStreamCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiStreamResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiStreamTransport;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamAgentExecutorFactoryTest {

    @Test
    void create_should_build_executable_stream_pipeline() {
        OpenAiStreamAgentExecutorFactory factory = new OpenAiStreamAgentExecutorFactory(
                new OpenAiChatRequestMapper(),
                new OpenAiAdvisorsSupport(),
                new OpenAiStreamTransport(createStubWebClient()),
                new OpenAiStreamResponseMapper()
        );

        StreamAgentExecutor<OpenAiStreamCompletionResponse> executor = factory.create();
        StreamAgentContext<OpenAiStreamCompletionResponse> context =
                StreamAgentContext.create(createRequest());

        List<OpenAiStreamCompletionResponse> results =
                executor.execute(context).collectList().block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, context.getRounds().size());
        assertNotNull(context.getTerminationReason());
    }

    @Test
    void constructor_should_validate_arguments() {
        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiAdvisorsSupport clientSupport = new OpenAiAdvisorsSupport();
        OpenAiStreamTransport transport = new OpenAiStreamTransport(createStubWebClient());
        OpenAiStreamResponseMapper responseMapper = new OpenAiStreamResponseMapper();

        assertThrows(NullPointerException.class,
                () -> new OpenAiStreamAgentExecutorFactory(null, clientSupport, transport, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiStreamAgentExecutorFactory(requestMapper, null, transport, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiStreamAgentExecutorFactory(requestMapper, clientSupport, null, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiStreamAgentExecutorFactory(requestMapper, clientSupport, transport, null));
    }

    private static OpenAiChatCompletionRequest createRequest() {
        return OpenAiChatCompletionRequest.builder()
                .baseRequest(OpenAiBaseRequest.builder()
                        .baseUrl("https://api.example.com")
                        .apiKey("test-key")
                        .model("test-model")
                        .build())
                .chatRequest(ChatRequest.builder()
                        .addMessage(Messages.user("hello"))
                        .build())
                .build();
    }

    private static WebClient createStubWebClient() {
        String sseData = """
                {"id":"resp-1","object":"chat.completion.chunk","created":123,"model":"test-model","choices":[{"index":0,"delta":{"role":"assistant","content":"hello"},"finish_reason":"stop"}]}""";

        ServerSentEvent<String> sseEvent = ServerSentEvent.<String>builder().data(sseData).build();

        WebClient.ResponseSpec responseSpec = (WebClient.ResponseSpec) Proxy.newProxyInstance(
                OpenAiStreamAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.ResponseSpec.class},
                (proxy, method, args) -> {
                    if ("bodyToFlux".equals(method.getName())) {
                        return Flux.just(sseEvent);
                    }
                    if (method.getReturnType().isInstance(proxy)) {
                        return proxy;
                    }
                    return null;
                }
        );

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = (WebClient.RequestHeadersSpec<?>) Proxy.newProxyInstance(
                OpenAiStreamAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.RequestBodyUriSpec.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "uri", "header", "accept", "bodyValue" -> {
                            return proxy;
                        }
                        case "retrieve" -> {
                            return responseSpec;
                        }
                        default -> {
                            if (method.getReturnType().isInstance(proxy)) {
                                return proxy;
                            }
                            return null;
                        }
                    }
                }
        );

        return (WebClient) Proxy.newProxyInstance(
                OpenAiStreamAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.class},
                (proxy, method, args) -> {
                    if ("post".equals(method.getName())) {
                        return requestHeadersSpec;
                    }
                    return null;
                }
        );
    }
}
