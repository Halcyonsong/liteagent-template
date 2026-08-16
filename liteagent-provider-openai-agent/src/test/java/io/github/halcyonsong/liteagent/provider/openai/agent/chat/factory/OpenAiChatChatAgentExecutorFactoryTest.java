package io.github.halcyonsong.liteagent.provider.openai.agent.chat.factory;

import io.github.halcyonsong.liteagent.agent.chat.context.ChatAgentContext;
import io.github.halcyonsong.liteagent.agent.chat.executor.ChatAgentExecutor;
import io.github.halcyonsong.liteagent.core.message.type.constructor.Messages;
import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiAdvisorsExecutor;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiBaseRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiChatChatAgentExecutorFactoryTest {

    @Test
    void create_should_build_executable_pipeline() {
        OpenAiChatAgentExecutorFactory factory = new OpenAiChatAgentExecutorFactory(
                new OpenAiChatRequestMapper(),
                new OpenAiAdvisorsExecutor(),
                new OpenAiChatTransport(createStubWebClient()),
                new OpenAiChatResponseMapper()
        );

        ChatAgentExecutor executor = factory.create();
        ChatAgentContext context = ChatAgentContext.create(createRequest());

        ChatAgentContext result = executor.execute(context);

        assertNotNull(result.getResult());
        assertNotNull(result.getTerminationReason());
    }

    @Test
    void constructor_should_validate_arguments() {
        OpenAiChatRequestMapper requestMapper = new OpenAiChatRequestMapper();
        OpenAiAdvisorsExecutor clientSupport = new OpenAiAdvisorsExecutor();
        OpenAiChatTransport transport = new OpenAiChatTransport(createStubWebClient());
        OpenAiChatResponseMapper responseMapper = new OpenAiChatResponseMapper();

        assertThrows(NullPointerException.class,
                () -> new OpenAiChatAgentExecutorFactory(null, clientSupport, transport, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiChatAgentExecutorFactory(requestMapper, null, transport, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiChatAgentExecutorFactory(requestMapper, clientSupport, null, responseMapper));
        assertThrows(NullPointerException.class,
                () -> new OpenAiChatAgentExecutorFactory(requestMapper, clientSupport, transport, null));
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
        OpenAiChatCompletionRawResponse rawResponse = createRawResponse();

        WebClient.ResponseSpec responseSpec = (WebClient.ResponseSpec) Proxy.newProxyInstance(
                OpenAiChatChatAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.ResponseSpec.class},
                (proxy, method, args) -> {
                    if ("bodyToMono".equals(method.getName())) {
                        return Mono.just(rawResponse);
                    }
                    if (method.getReturnType().isInstance(proxy)) {
                        return proxy;
                    }
                    return null;
                }
        );

        WebClient.RequestHeadersSpec<?> requestHeadersSpec = (WebClient.RequestHeadersSpec<?>) Proxy.newProxyInstance(
                OpenAiChatChatAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.RequestBodyUriSpec.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "uri" -> {
                            assertTrue(String.valueOf(args[0]).endsWith("/v1/chat/completions"));
                            return proxy;
                        }
                        case "header" -> {
                            String headerName = (String) args[0];
                            String[] headerValues = (String[]) args[1];
                            assertEquals(1, headerValues.length);

                            if (HttpHeaders.AUTHORIZATION.equals(headerName)) {
                                assertEquals("Bearer test-key", headerValues[0]);
                            } else if (HttpHeaders.CONTENT_TYPE.equals(headerName)) {
                                assertEquals(MediaType.APPLICATION_JSON_VALUE, headerValues[0]);
                            }

                            return proxy;
                        }
                        case "bodyValue" -> {
                            OpenAiChatCompletionRawRequest rawRequest = (OpenAiChatCompletionRawRequest) args[0];
                            assertEquals("test-model", rawRequest.getModel());
                            assertEquals(1, rawRequest.getMessages().size());
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
                OpenAiChatChatAgentExecutorFactoryTest.class.getClassLoader(),
                new Class[]{WebClient.class},
                (proxy, method, args) -> {
                    if ("post".equals(method.getName())) {
                        return requestHeadersSpec;
                    }
                    return null;
                }
        );
    }

    private static OpenAiChatCompletionRawResponse createRawResponse() {
        OpenAiChatCompletionRawResponse rawResponse = new OpenAiChatCompletionRawResponse();
        rawResponse.setId("resp-1");
        rawResponse.setObject("chat.completion");
        rawResponse.setCreated(123L);
        rawResponse.setModel("test-model");

        OpenAiChatCompletionRawResponse.RawMessage rawMessage =
                new OpenAiChatCompletionRawResponse.RawMessage();
        rawMessage.setRole("assistant");
        rawMessage.setContent("hello");

        OpenAiChatCompletionRawResponse.RawChoice rawChoice =
                new OpenAiChatCompletionRawResponse.RawChoice();
        rawChoice.setIndex(0);
        rawChoice.setMessage(rawMessage);
        rawChoice.setFinishReason("stop");

        rawResponse.setChoices(List.of(rawChoice));
        return rawResponse;
    }
}