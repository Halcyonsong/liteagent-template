package io.github.halcyonsong.liteagent.core.message;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;

import java.util.Objects;

public abstract class AbstractMessage implements Message {

    private final MessageRole role;
    private final String content;

    protected AbstractMessage(MessageRole role, String content) {
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    @Override
    public MessageRole getRole() {
        return role;
    }

    @Override
    public String getContent() {
        return content;
    }
}