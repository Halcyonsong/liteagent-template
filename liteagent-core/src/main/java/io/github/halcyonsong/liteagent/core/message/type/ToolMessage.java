package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 工具执行结果消息。
 * <p>
 * toolCallId 用于将本条 tool 消息与上一轮 assistant 发出的具体 tool call 关联。
 */
@Getter
@ToString(callSuper = true)
public class ToolMessage extends AbstractMessage {

    private final String toolCallId;

    public ToolMessage(String content, String toolCallId) {
        super(MessageRole.TOOL, content);
        this.toolCallId = Objects.requireNonNull(toolCallId, "toolCallId must not be null");
    }

}