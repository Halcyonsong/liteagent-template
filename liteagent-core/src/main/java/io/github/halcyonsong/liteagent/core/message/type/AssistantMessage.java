package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

public class AssistantMessage extends AbstractMessage {

    public AssistantMessage(String content) {
        super(MessageRole.ASSISTANT, content);
    }
}