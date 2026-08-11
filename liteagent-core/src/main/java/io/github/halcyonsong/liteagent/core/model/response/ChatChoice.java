package io.github.halcyonsong.liteagent.core.model.response;

import io.github.halcyonsong.liteagent.core.model.enums.FinishReason;

import java.util.Objects;

/**
 * 单条候选聊天结果。
 * <p>
 * 该对象对应模型响应中的一条 choice，
 * 包含其在结果列表中的索引、实际聊天响应内容以及结束原因。
 */
public class ChatChoice {

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

    public Integer getIndex() {
        return index;
    }

    public ChatResponse getChatResponse() {
        return chatResponse;
    }

    public FinishReason getFinishReason() {
        return finishReason;
    }
}