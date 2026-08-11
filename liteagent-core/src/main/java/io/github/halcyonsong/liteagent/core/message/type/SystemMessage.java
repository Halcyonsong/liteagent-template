package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

public final class SystemMessage extends AbstractMessage {

    public SystemMessage(String content) {
        super(MessageRole.SYSTEM, content);
    }
}