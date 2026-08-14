package io.github.halcyonsong.liteagent.core.model.request.norm;

import io.github.halcyonsong.liteagent.core.model.request.impl.ChatRequest;

public interface Invocation {
    BaseRequest getBaseRequest();
    ChatRequest getChatRequest();
}
