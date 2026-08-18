package io.github.halcyonsong.liteagent.provider.openai.transport;

import io.github.halcyonsong.liteagent.core.exception.ErrorCode;
import io.github.halcyonsong.liteagent.core.exception.ModelException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * 将 HTTP / 网络异常映射为带 ErrorCode 的 ModelException。
 */
final class OpenAiErrorClassifier {

    private OpenAiErrorClassifier() {}

    /** 根据 HTTP 状态码和异常信息构建分类异常。 */
    static ModelException classify(WebClientResponseException e, String endpoint) {
        int status = e.getStatusCode().value();
        ErrorCode code = mapHttpStatus(status);
        String msg = "OpenAI API error: status=" + status
                + ", endpoint=" + endpoint
                + ", body=" + e.getResponseBodyAsString();
        return new ModelException(code, msg, e);
    }

    /** 将非 HTTP 异常（连接超时、DNS 失败等）映射为分类异常。 */
    static ModelException classify(Exception e, String endpoint) {
        ErrorCode code = mapNetworkException(e);
        String msg = "Failed to call OpenAI API: endpoint=" + endpoint;
        return new ModelException(code, msg, e);
    }

    private static ErrorCode mapHttpStatus(int status) {
        if (status == 401 || status == 403) return ErrorCode.MODEL_AUTH_ERROR;
        if (status == 404) return ErrorCode.MODEL_ROUTING_ERROR;
        if (status == 408) return ErrorCode.MODEL_TIMEOUT_ERROR;
        if (status == 429) return ErrorCode.MODEL_RATE_LIMIT_ERROR;
        if (status >= 400 && status < 500) return ErrorCode.MODEL_REQUEST_ERROR;
        if (status >= 500) return ErrorCode.MODEL_SERVER_ERROR;
        return ErrorCode.UNKNOWN;
    }

    private static ErrorCode mapNetworkException(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof SocketTimeoutException || cur instanceof TimeoutException) {
                return ErrorCode.MODEL_TIMEOUT_ERROR;
            }
            if (cur instanceof ConnectException || cur instanceof UnknownHostException) {
                return ErrorCode.MODEL_NETWORK_ERROR;
            }
            cur = cur.getCause();
        }
        return ErrorCode.MODEL_NETWORK_ERROR; // 安全兜底
    }
}