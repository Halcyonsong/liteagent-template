package io.github.halcyonsong.liteagent.core.message;

import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public abstract class AbstractMessage implements Message {

    private final MessageRole role;
    private final String content;

    protected AbstractMessage(MessageRole role, String content) {
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

}