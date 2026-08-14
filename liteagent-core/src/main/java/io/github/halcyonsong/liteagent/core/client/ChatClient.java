package io.github.halcyonsong.liteagent.core.client;

import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.Result;

/**
 * 框架统一普通对话客户端接口。
 * <p>
 * 用于约束所有 provider 在普通对话场景下的统一调用入口。
 */
public interface ChatClient {

    /**
     * 发起一次普通对话调用，并返回统一结果抽象。
     *
     * @param invocation 统一调用对象
     * @return 统一结果抽象
     */
    Result chat(Invocation invocation);
}