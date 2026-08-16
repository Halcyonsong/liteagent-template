package io.github.halcyonsong.liteagent.core.message.type;

import io.github.halcyonsong.liteagent.core.message.norm.AbstractMessage;
import io.github.halcyonsong.liteagent.core.message.enums.MessageRole;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * 工具执行结果消息。
 * <p>
 * toolCallId 用于回指上一轮 assistant 发出的 tool call。
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