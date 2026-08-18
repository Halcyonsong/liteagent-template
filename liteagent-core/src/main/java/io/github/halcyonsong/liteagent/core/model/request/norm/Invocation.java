package io.github.halcyonsong.liteagent.core.model.request.norm;

import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

/**
 * 一次模型调用的统一入参，由基础请求和聊天内容组成。
 */
public interface Invocation {
    BaseRequest getBaseRequest();
    ChatRequest getChatRequest();

    /**
     * 会话标识，随请求传递。null 表示本次调用不关联会话。
     */
    default String getSessionId() {
        return getChatRequest().getSessionId();
    }
}
