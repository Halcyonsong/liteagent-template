package io.github.halcyonsong.liteagent.core.client;

import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.stream.StreamChunk;
import reactor.core.publisher.Flux;

/**
 * 框架统一流式对话客户端接口。
 * <p>
 * 用于约束所有 provider 在流式对话场景下的统一调用入口。
 */
public interface StreamClient {

    /**
     * 发起一次流式对话调用，并返回框架统一流式结果。
     *
     * @param invocation 统一聊天调用对象
     * @return 框架统一流式结果流
     */
    Flux<StreamChunk> stream(ChatInvocation invocation);
}