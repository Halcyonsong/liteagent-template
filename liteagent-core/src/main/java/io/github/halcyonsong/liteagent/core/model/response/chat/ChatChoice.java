package io.github.halcyonsong.liteagent.core.model.response.chat;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;
import io.github.halcyonsong.liteagent.core.support.JsonSerializable;
import lombok.Getter;

import java.util.Objects;

/**
 * 单条候选聊天结果。
 * <p>
 * 该对象对应模型响应中的一条 choice，
 * 包含其在结果列表中的索引、实际聊天响应内容以及结束原因。
 */
@Getter
public class ChatChoice implements JsonSerializable {

    /**
     * 当前候选结果在响应 choices 中的索引位置。
     */
    private final Integer index;

    /**
     * 当前候选结果对应的聊天响应内容。
     */
    private final ChatResponse chatResponse;

    /**
     * 当前候选结果的生成结束原因。
     */
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