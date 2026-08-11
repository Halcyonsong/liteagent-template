package io.github.halcyonsong.liteagent.core.exception;

/**
 * 工具执行过程中的统一异常。
 */
public class ToolExecutionException extends LiteAgentException {

    public ToolExecutionException(String message) {
        super(ErrorCode.TOOL_EXECUTION_ERROR, message);
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(ErrorCode.TOOL_EXECUTION_ERROR, message, cause);
    }

    public ToolExecutionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ToolExecutionException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}