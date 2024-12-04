package github.ag777.util.remote.ollama.okhttp;

/**
 * 流式响应处理器接口
 * @author ag777
 */
public interface StreamResponseHandler<T> {
    /**
     * 处理响应
     * @param response 响应对象
     */
    void onResponse(T response);

    /**
     * 处理进度
     * @param current 当前进度
     * @param total 总进度，-1表示未知
     */
    default void onProgress(long current, long total) {
        // 默认空实现
    }

    /**
     * 处理完成
     * @param totalTokens 总token数
     */
    default void onComplete(long totalTokens) {
        // 默认空实现
    }

    /**
     * 处理错误
     * @param message 错误信息
     * @param t 异常对象
     */
    default void onError(String message, Throwable t) {
        // 默认空实现
    }
}
