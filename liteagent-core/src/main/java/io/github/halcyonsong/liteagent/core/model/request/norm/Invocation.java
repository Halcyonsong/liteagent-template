package io.github.halcyonsong.liteagent.core.model.request.norm;

import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

/**
 * 一次模型调用的统一入参抽象。
 * <p>
 * 由基础请求和聊天内容组成，供 provider 层实现。
 */
public interface Invocation {
    BaseRequest getBaseRequest();
    ChatRequest getChatRequest();
}
