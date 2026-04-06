package github.ag777.util.remote.ai.http.exception;

import lombok.Getter;

/**
 * AI HTTP调用异常。
 * 
 * <p>用于表示AI模型HTTP调用过程中发生的异常，尽量保留上下文信息，
 * 方便排查问题。包含HTTP状态码、响应体、请求URL等关键信息。
 * 
 * <p>该异常继承自{@link RuntimeException}，可以在需要时选择捕获或向上抛出。
 * 
 * @author ag777
 * @since 1.0
 */
@Getter
public class AiHttpException extends RuntimeException {
    private final Integer statusCode;
    private final String responseBody;
    private final String requestUrl;

    /**
     * 创建只包含消息的异常。
     * 
     * @param message 异常消息
     */
    public AiHttpException(String message) {
        this(message, null, null, null, null);
    }

    /**
     * 创建包含消息和原因的异常。
     * 
     * @param message 异常消息
     * @param cause 异常原因
     */
    public AiHttpException(String message, Throwable cause) {
        this(message, null, null, null, cause);
    }

    /**
     * 创建包含HTTP信息的异常。
     * 
     * @param message 异常消息
     * @param statusCode HTTP状态码
     * @param responseBody 响应体内容
     * @param requestUrl 请求URL
     */
    public AiHttpException(String message, Integer statusCode, String responseBody, String requestUrl) {
        this(message, statusCode, responseBody, requestUrl, null);
    }

    /**
     * 创建包含完整信息的异常。
     * 
     * @param message 异常消息
     * @param statusCode HTTP状态码
     * @param responseBody 响应体内容
     * @param requestUrl 请求URL
     * @param cause 异常原因
     */
    public AiHttpException(String message, Integer statusCode, String responseBody, String requestUrl, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.requestUrl = requestUrl;
    }
}
