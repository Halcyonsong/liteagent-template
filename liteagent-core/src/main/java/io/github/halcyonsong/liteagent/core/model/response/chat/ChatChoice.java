package io.github.halcyonsong.liteagent.core.model.response.chat;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;

import java.util.Objects;

/**
 * 单条候选聊天结果，对应模型响应中的一条 choice。
 */
@Getter
public class ChatChoice implements JsonSerializable {

    private final Integer index;
    private final ChatResponse chatResponse;
    private final FinishReason finishReason;

    public ChatChoice(Integer index, ChatResponse chatResponse, FinishReason finishReason) {
        this.index = index;
        this.chatResponse = Objects.requireNonNull(chatResponse, "chatResponse must not be null");
        this.finishReason = finishReason;
    }

    @Override
    public String toString() {
        return "ChatChoice{" +
                "index=" + index +
                ", finishReason=" + finishReason +
                ", messageCount=" + chatResponse.getMessages().size() +
                '}';
    }
}