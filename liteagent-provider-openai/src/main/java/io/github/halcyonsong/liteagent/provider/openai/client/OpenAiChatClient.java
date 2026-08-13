package io.github.halcyonsong.liteagent.provider.openai.client;

import io.github.halcyonsong.liteagent.core.client.ChatClient;
import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;
import io.github.halcyonsong.liteagent.provider.openai.request.config.OpenAiChatCompletionRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.quickrequest.OpenAiQuickChatRequest;
import io.github.halcyonsong.liteagent.provider.openai.request.mapper.OpenAiChatRequestMapper;
import io.github.halcyonsong.liteagent.provider.openai.request.raw.OpenAiChatCompletionRawRequest;
import io.github.halcyonsong.liteagent.provider.openai.response.config.chat.OpenAiChatCompletionResponse;
import io.github.halcyonsong.liteagent.provider.openai.response.mapper.OpenAiChatResponseMapper;
import io.github.halcyonsong.liteagent.provider.openai.response.raw.OpenAiChatCompletionRawResponse;
import io.github.halcyonsong.liteagent.provider.openai.support.OpenAiEndpointResolver;
import io.github.halcyonsong.liteagent.provider.openai.transport.OpenAiChatTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

/**
 * OpenAI-compatible 对话客户端。
 * <p>
 * 负责上层请求编排、raw request/raw response 转换以及统一结果输出。
 */
@Slf4j
public class OpenAiChatClient implements ChatClient {

    private final OpenAiChatRequestMapper requestMapper;
    private final OpenAiChatResponseMapper responseMapper;
    private final OpenAiChatTransport transport;

    public OpenAiChatClient(WebClient webClient) {
        this.requestMapper = new OpenAiChatRequestMapper();
        this.responseMapper = new OpenAiChatResponseMapper();
        this.transport = new OpenAiChatTransport(webClient);
    }

    public OpenAiChatClient(OpenAiChatRequestMapper requestMapper,
                            OpenAiChatResponseMapper responseMapper,
                            OpenAiChatTransport transport) {
        this.requestMapper = Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
    }

    /**
     * 使用统一调用对象发起普通对话请求，并返回框架统一响应结果。
     * <p>
     * 该方法面向希望使用 core 层统一抽象的调用方，
     * 会在内部自动完成 provider 请求构造、HTTP 调用以及响应降级映射。
     *
     * @param invocation 统一聊天调用对象
     * @return 框架统一聊天结果
     */
    @Override
    public ChatResult chat(ChatInvocation invocation) {
        log.debug("Chat invoked by ChatInvocation. messageCount={}, hasChatOptions={}",
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);
        return chatCompletion(invocation).toChatResult();
    }

    /**
     * 使用 provider 请求对象发起普通对话请求，并返回 provider 响应包装对象。
     * <p>
     * 该方法适合需要保留 OpenAI-compatible 协议扩展字段的调用方。
     *
     * @param request OpenAI-compatible provider 请求对象
     * @return provider 层响应包装对象
     */
    public OpenAiChatCompletionResponse chat(OpenAiChatCompletionRequest request) {
        log.debug("Chat invoked by OpenAiChatCompletionRequest. model={}",
                request.getBaseRequest().getModel());
        return chatCompletion(request);
    }

    /**
     * 使用统一调用对象发起普通对话请求，并返回 provider 响应包装对象。
     * <p>
     * 该方法会先将统一调用对象转换为 provider 请求对象，
     * 再执行普通 completion 请求。
     *
     * @param invocation 统一聊天调用对象
     * @return provider 层普通 completion 响应
     */
    public OpenAiChatCompletionResponse chatCompletion(ChatInvocation invocation) {
        log.debug("Building OpenAI completion request from ChatInvocation. model={}, messageCount={}, hasChatOptions={}",
                invocation.getBaseRequest().getModel(),
                invocation.getChatRequest().getMessages().size(),
                invocation.getChatOptions() != null);

        OpenAiChatCompletionRequest request = OpenAiChatCompletionRequest.builder()
                .baseRequest(invocation.getBaseRequest())
                .chatRequest(invocation.getChatRequest())
                .completionOptions(requestMapper.toCompletionOptions(invocation.getChatOptions()))
                .build();

        return chatCompletion(request);
    }

    /**
     * 使用 provider 请求对象执行普通 completion 请求。
     * <p>
     * 该方法负责：
     * <ul>
     *   <li>将 provider 请求映射为 raw request</li>
     *   <li>设置 stream=false</li>
     *   <li>规范化请求地址</li>
     *   <li>发送 HTTP 请求并映射响应</li>
     * </ul>
     *
     * @param request OpenAI-compatible provider 请求对象
     * @return provider 层普通 completion 响应
     */
    public OpenAiChatCompletionResponse chatCompletion(OpenAiChatCompletionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        OpenAiChatCompletionRawRequest rawRequest = requestMapper.toRawRequest(request);
        rawRequest.setStream(false);
        String endpoint = OpenAiEndpointResolver.resolveChatCompletionsEndpoint(
                request.getBaseRequest().getBaseUrl()
        );
        String apiKey = request.getBaseRequest().getApiKey();

        log.debug("Prepared OpenAI-compatible request. model={}, endpoint={}, messageCount={}, stream={}",
                rawRequest.getModel(),
                endpoint,
                rawRequest.getMessages() == null ? 0 : rawRequest.getMessages().size(),
                rawRequest.getStream());

        OpenAiChatCompletionRawResponse rawResponse = transport.send(endpoint, apiKey, rawRequest);
        OpenAiChatCompletionResponse response = responseMapper.fromRaw(rawResponse);

        log.debug("Mapped OpenAI-compatible response. responseId={}, choiceCount={}, model={}",
                response.getBaseResponse().getId(),
                response.getChoices().size(),
                response.getBaseResponse().getModel());

        return response;
    }

    /**
     * 使用快速请求对象发起普通对话请求，并返回框架统一响应结果。
     * <p>
     * 适合快速测试和最小调用场景。
     *
     * @param request 快速聊天请求对象
     * @return 框架统一聊天结果
     */
    public ChatResult chat(OpenAiQuickChatRequest request) {
        log.debug("Chat invoked by OpenAiQuickChatRequest. model={}, hasSystemMessage={}",
                request.getModel(),
                request.getSystemMessage() != null && !request.getSystemMessage().isBlank());
        return chat(request.toInvocation());
    }

    /**
     * 使用快速请求对象发起普通对话请求，并返回 provider 响应包装对象。
     *
     * @param request 快速聊天请求对象
     * @return provider 层普通 completion 响应
     */
    public OpenAiChatCompletionResponse chatCompletion(OpenAiQuickChatRequest request) {
        log.debug("ChatCompletion invoked by OpenAiQuickChatRequest. model={}, userMessageLength={}",
                request.getModel(),
                request.getUserMessage() == null ? 0 : request.getUserMessage().length());
        return chatCompletion(request.toInvocation());
    }

}