package io.github.halcyonsong.liteagent.core.message.norm;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

public interface Message {

    MessageRole getRole();

    String getContent();
}