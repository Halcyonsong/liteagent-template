package io.github.halcyonsong.liteagent.core.message.type.constructor;

import io.github.halcyonsong.liteagent.core.message.type.AssistantMessage;
import io.github.halcyonsong.liteagent.core.message.type.SystemMessage;
import io.github.halcyonsong.liteagent.core.message.type.ToolMessage;
import io.github.halcyonsong.liteagent.core.message.type.UserMessage;

public final class Messages {

    private Messages() {
    }

    public static UserMessage user(String content) {
        return new UserMessage(content);
    }

    public static AssistantMessage assistant(String content) {
        return new AssistantMessage(content);
    }

    public static SystemMessage system(String content) {
        return new SystemMessage(content);
    }

    public static ToolMessage tool(String content) {
        return new ToolMessage(content);
    }
}