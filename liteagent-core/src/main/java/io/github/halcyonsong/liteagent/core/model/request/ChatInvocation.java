package io.github.halcyonsong.liteagent.core.model.request;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

/**
 * 统一的聊天调用聚合对象。
 * <p>
 * 该对象将一次调用所需的基础请求信息、聊天消息内容以及通用可选参数组合在一起，
 * 作为 provider 层执行实际模型调用时的统一输入。
 */
@Getter
@Builder
public class ChatInvocation {

    private final BaseRequest baseRequest;
    private final ChatRequest chatRequest;
    private final ChatOptions chatOptions;

    private ChatInvocation(Builder builder) {
        this.baseRequest = Objects.requireNonNull(builder.baseRequest, "baseRequest must not be null");
        this.chatRequest = Objects.requireNonNull(builder.chatRequest, "chatRequest must not be null");
        this.chatOptions = builder.chatOptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private BaseRequest baseRequest;
        private ChatRequest chatRequest;
        private ChatOptions chatOptions;

        public Builder baseRequest(BaseRequest baseRequest) {
            this.baseRequest = baseRequest;
            return this;
        }

        public Builder chatRequest(ChatRequest chatRequest) {
            this.chatRequest = chatRequest;
            return this;
        }

        public Builder chatOptions(ChatOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }

        public ChatInvocation build() {
            return new ChatInvocation(this);
        }
    }
}