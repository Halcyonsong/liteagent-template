package io.github.halcyonsong.liteagent.core.exception;

/**
 * 模型调用过程中的统一异常。
 */
public class ModelException extends LiteAgentException {

    public ModelException(String message) {
        super(ErrorCode.MODEL_RESPONSE_ERROR, message);
    }

    public ModelException(String message, Throwable cause) {
        super(ErrorCode.MODEL_RESPONSE_ERROR, message, cause);
    }

    public ModelException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ModelException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}