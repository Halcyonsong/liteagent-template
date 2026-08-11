package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

public class ToolMessage extends AbstractMessage {

    public ToolMessage(String content) {
        super(MessageRole.TOOL, content);
    }
}