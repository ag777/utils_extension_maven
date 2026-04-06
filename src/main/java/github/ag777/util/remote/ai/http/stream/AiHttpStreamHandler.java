package github.ag777.util.remote.ai.http.stream;

import github.ag777.util.remote.ai.http.model.AiHttpChunk;
import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;
import github.ag777.util.remote.ai.http.model.AiHttpToolCallDelta;

/**
 * 流式事件处理器接口。
 * 
 * <p>用于处理AI模型流式响应过程中的各种事件，包括开始、内容增量、
 * 推理增量、工具调用、分片接收、完成和错误等事件。
 * 
 * <p>该接口的所有方法都有默认实现，用户可以根据需要选择性重写。
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpStreamHandler handler = new AiHttpStreamHandler() {
 *     @Override
 *     public void onContent(String delta, AiHttpChunk chunk) {
 *         System.out.println("收到内容: " + delta);
 *     }
 *     
 *     @Override
 *     public void onComplete(AiHttpResponse response) {
 *         System.out.println("响应完成: " + response.content());
 *     }
 * };
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
public interface AiHttpStreamHandler {

    /**
     * 当流式请求开始时调用。
     * 
     * @param request 发送的请求对象
     */
    default void onStart(AiHttpRequest request) {
    }

    /**
     * 当收到推理内容增量时调用。
     * 
     * @param delta 推理内容的增量文本
     * @param chunk 包含该增量的完整分片对象
     */
    default void onReasoning(String delta, AiHttpChunk chunk) {
    }

    /**
     * 当收到对话内容增量时调用。
     * 
     * @param delta 对话内容的增量文本
     * @param chunk 包含该增量的完整分片对象
     */
    default void onContent(String delta, AiHttpChunk chunk) {
    }

    /**
     * 当收到工具调用增量时调用。
     * 
     * @param delta 工具调用的增量信息
     * @param chunk 包含该增量的完整分片对象
     */
    default void onToolCall(AiHttpToolCallDelta delta, AiHttpChunk chunk) {
    }

    /**
     * 当收到任意分片时调用（无论分片内容）。
     * 
     * @param chunk 收到的分片对象
     */
    default void onChunk(AiHttpChunk chunk) {
    }

    /**
     * 当流式响应完成时调用。
     * 
     * @param response 组装完成的响应对象
     */
    default void onComplete(AiHttpResponse response) {
    }

    /**
     * 当流式处理过程中发生错误时调用。
     * 
     * @param throwable 发生的异常
     */
    default void onError(Throwable throwable) {
    }
}
