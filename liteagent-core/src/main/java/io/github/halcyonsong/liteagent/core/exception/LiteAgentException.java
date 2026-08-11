package io.github.halcyonsong.liteagent.core.exception;

/**
 * LiteAgent 框架运行时异常基类。
 */
public class LiteAgentException extends RuntimeException {

    private final ErrorCode errorCode;

    public LiteAgentException(String message) {
        this(ErrorCode.UNKNOWN, message, null);
    }

    public LiteAgentException(String message, Throwable cause) {
        this(ErrorCode.UNKNOWN, message, cause);
    }

    public LiteAgentException(Throwable cause) {
        this(ErrorCode.UNKNOWN, cause == null ? null : cause.getMessage(), cause);
    }

    public LiteAgentException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public LiteAgentException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode == null ? ErrorCode.UNKNOWN : errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}