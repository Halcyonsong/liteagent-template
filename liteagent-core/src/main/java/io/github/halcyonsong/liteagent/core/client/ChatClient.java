package io.github.halcyonsong.liteagent.core.client;

import io.github.halcyonsong.liteagent.core.model.request.ChatInvocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.ChatResult;

/**
 * 框架统一普通对话客户端接口。
 * <p>
 * 用于约束所有 provider 在普通对话场景下的统一调用入口。
 */
public interface ChatClient {

    /**
     * 发起一次普通对话调用，并返回框架统一聊天结果。
     *
     * @param invocation 统一聊天调用对象
     * @return 框架统一聊天结果
     */
    ChatResult chat(ChatInvocation invocation);
}