package io.github.halcyonsong.liteagent.agent.chat.context;

import io.github.halcyonsong.liteagent.agent.state.AgentTerminationReason;
import io.github.halcyonsong.liteagent.core.message.Message;
import io.github.halcyonsong.liteagent.core.model.request.norm.Invocation;
import io.github.halcyonsong.liteagent.core.model.response.chat.Result;
import io.github.halcyonsong.liteagent.core.tool.norm.ToolRegistry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单次同步 chat 编排调用的上下文。
 * <p>
 * 该对象的生命周期限定在一次 execute 调用内部，
 * 不是全局共享状态，也不会跨请求复用。
 * <p>
 * 它主要承载四类数据：
 * 1. 本次调用输入 invocation
 * 2. 工作态消息历史 workingMessages，用于轮次间继续发给模型
 * 3. 执行控制状态：iteration、maxIterations、terminationReason
 * 4. 跨步骤共享的扩展数据 attributes
 * <p>
 * provider-specific 的中间产物不直接定义为固定字段，
 * 而是优先通过 attributes 扩展槽存储。
 */
@Getter
public class ChatAgentContext {

    /**
     * 本次调用的唯一执行标识。
     * <p>
     * 主要用于后续日志串联、trace、checkpoint 或故障排查。
     */
    private final String executionId;

    /**
     * 本次调用的统一输入对象。
     * <p>
     * 该字段在一次执行过程中保持不变，步骤应基于它构造 provider 请求。
     */
    private final Invocation invocation;

    /**
     * 当前编排可用的工具注册表。
     * <p>
     * 只在第 0 轮初始化阶段从工具 advisor 中提取一次，
     * 后续轮次直接复用。
     */
    @Setter
    private ToolRegistry toolRegistry;

    /**
     * 本次 chat 编排内部使用的工作态消息历史。
     * <p>
     * 该列表一般在初始化步骤中从初始请求复制一次，
     * 后续轮次只追加新的 assistant、tool 或其他工作消息。
     */
    private final List<Message> workingMessages = new ArrayList<>();

    /**
     * 跨步骤共享的扩展数据槽。
     * <p>
     * 适合存放 provider 中间态、调试标记、hook 临时数据等不宜进入通用字段的内容。
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * 本次调用的最终结果。
     * <p>
     * 由结果构建步骤在流程末端写入。
     */
    @Setter
    private Result result;

    /**
     * 当前已进入的模型调用轮次。
     * <p>
     * 主要用于工具调用回环控制和防止无限循环。
     */
    private int iteration;

    /**
     * 允许的最大模型调用轮次。
     * <p>
     * 当自动工具执行接入后，可用于限制回环次数。
     */
    private int maxIterations = 10;

    /**
     * 本次执行的结束原因。
     * <p>
     * 用于区分正常完成、达到轮次上限、模型错误、工具执行错误等不同终态。
     */
    @Setter
    private AgentTerminationReason terminationReason;

    private ChatAgentContext(String executionId, Invocation invocation) {
        this.executionId = executionId;
        this.invocation = invocation;
    }

    public static ChatAgentContext create(Invocation invocation) {
        return create(UUID.randomUUID().toString(), invocation);
    }

    public static ChatAgentContext create(String executionId, Invocation invocation) {
        if (executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("executionId must not be blank");
        }
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        return new ChatAgentContext(executionId, invocation);
    }

    /**
     * 清空当前工作态消息历史。
     */
    public void clearWorkingMessages() {
        this.workingMessages.clear();
    }

    /**
     * 向工作态消息历史追加一条消息。
     */
    public void appendWorkingMessage(Message message) {
        this.workingMessages.add(Objects.requireNonNull(message, "message must not be null"));
    }

    /**
     * 向工作态消息历史追加多条消息。
     */
    public void appendWorkingMessages(List<? extends Message> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        this.workingMessages.addAll(messages);
    }

    /**
     * 写入或覆盖一个扩展属性。
     * <p>
     * 当 {@code value} 为 {@code null} 时，会删除对应键。
     *
     * @param key 扩展属性键
     * @param value 扩展属性值，允许为 null
     */
    public void setAttribute(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("attribute key must not be blank");
        }
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 以强类型方式读取扩展属性。
     *
     * @param key 属性键
     * @param type 目标类型
     * @param <T> 目标类型参数
     * @return 类型转换后的属性值；不存在时返回 null
     */
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value == null ? null : type.cast(value);
    }

    /**
     * 将当前执行轮次加一。
     * <p>
     * 一般在完成一轮工具执行并准备重新请求模型时调用。
     */
    public void incrementIteration() {
        this.iteration++;
    }

    /**
     * 设置允许的最大模型调用轮次。
     * <p>
     * 主要用于工具调用回环控制，避免无限循环。
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 1) {
            throw new IllegalArgumentException("maxIterations must be greater than zero");
        }
        this.maxIterations = maxIterations;
    }

}