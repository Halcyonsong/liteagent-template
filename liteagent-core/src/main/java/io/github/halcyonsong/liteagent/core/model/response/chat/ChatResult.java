package io.github.halcyonsong.liteagent.core.model.response.chat;

import io.github.halcyonsong.liteagent.core.model.response.BaseResponse;
import io.github.halcyonsong.liteagent.core.model.response.Usage;
import io.github.halcyonsong.liteagent.core.support.JsonSupport;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * 框架统一的聊天调用结果。
 * <p>
 * 该对象用于向上层暴露与供应商无关的统一响应结构，
 * 包括基础响应信息、候选结果列表以及 token 用量统计。
 */
@Getter
@ToString
public class ChatResult {

    private final BaseResponse baseResponse;
    private final List<ChatChoice> choices;
    private final Usage usage;

    public ChatResult(BaseResponse baseResponse, List<ChatChoice> choices, Usage usage) {
        this.baseResponse = Objects.requireNonNull(baseResponse, "baseResponse must not be null");
        Objects.requireNonNull(choices, "choices must not be null");
        this.choices = List.copyOf(choices);
        this.usage = usage;
    }

    public String toJson() {
        return JsonSupport.toJson(this);
    }

}