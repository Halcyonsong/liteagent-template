package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

public final class UserMessage extends AbstractMessage {

    public UserMessage(String content) {
        super(MessageRole.USER, content);
    }
}