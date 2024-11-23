package github.ag777.util.remote.ollama.spring.ai;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.spring.ai.model.AIModelReplyDeadLoopException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/11/19 下午5:01
 */
public class OllamaUtils {
    private static int repeatThresholdLength=0;

    public static void repeatThresholdLength(int repeatThresholdLength) {
        OllamaUtils.repeatThresholdLength = repeatThresholdLength;
    }

    public static int setRepeatThresholdLength() {
        return repeatThresholdLength;
    }

    /**
     * 向指定模型发送聊天消息并获取回复。
     *
     * @param modelName 模型名称，指定与之聊天的模型。
     * @param options 聊天选项，可包含各种配置参数。
     * @param messages 发送给模型的消息内容。
     * @return 从模型收到的回复内容。
     * @throws ValidateException 如果验证失败或聊天过程中发生异常。
     */
    public static String chat(OllamaApi api, String modelName, Map<String, Object> options, List<OllamaApi.Message> messages) throws ValidateException {
        return chat(api, modelName, options, null, messages);
    }

    public static String chat(OllamaApi api, String modelName, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, List<OllamaApi.Message> messages) throws ValidateException {
        OllamaApi.ChatRequest request = getRequest(modelName, messages, options, tools, false); // 构建聊天请求

        try {
            OllamaApi.ChatResponse response = api.chat(request); // 发送请求并获取回复
            // 提取回复内容
            return response.message().content(); // 返回回复内容
        } catch (Exception e) {
            throw new ValidateException(e.getMessage(), e); // 将异常封装并抛出
        }
    }

    /**
     * 异步聊天函数，发送消息并接收回复。
     *
     * @param modelName 要对话的模型名称。
     * @param options 聊天选项映射，可包含各种对话定制选项。
     * @param messages 要发送的消息内容。
     * @param onMessage 消息回调消费函数，用于处理接收到的每条消息。
     * @return CompletableFuture<String> 异步完成的未来对象，包含累计的回复内容。
     */
    public static CompletableFuture<String> chatAsync(OllamaApi api, String modelName, Map<String, Object> options, List<OllamaApi.Message> messages, OnMessage onMessage) {
        return chatAsync(api, modelName, options, null, messages, onMessage);
    }

    public static CompletableFuture<String> chatAsync(OllamaApi api, String modelName, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, List<OllamaApi.Message> messages, OnMessage onMessage) {
        return chatAsync(api, modelName, options, tools, messages, onMessage, repeatThresholdLength);
    }

    public static CompletableFuture<String> chatAsync(OllamaApi api, String modelName, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, List<OllamaApi.Message> messages, OnMessage onMessage, int repeatThresholdLength) {
        // 创建一个CompletableFuture，并注册完成时的处理逻辑
        CompletableFuture<String> future = new CompletableFuture<>();
        // 用于累积回复内容
        StringBuilder result = new StringBuilder();
        // 构建聊天请求
        OllamaApi.ChatRequest request = getRequest(modelName, messages, options, tools, true);

        // 发送异步聊天请求并订阅响应
        Flux<OllamaApi.ChatResponse> response = api.streamingChat(request)
                .onErrorStop() // 错误时停止订阅
                .handle((data, sink) -> {
                    // 如果Future被取消，向sink发送错误并终止处理
                    if (future.isCancelled()) {
                        sink.error(new InterruptedException("请求中断"));
                        return;
                    }
                    // 异常
                    if (future.isCompletedExceptionally()) {
                        sink.error(future.exceptionNow());
                        return;
                    }
                    // 死循环判断
                    if (repeatThresholdLength>0) {
                        try {
                            AIModelReplyDeadLoopChecker.test(result, repeatThresholdLength);
                        } catch (AIModelReplyDeadLoopException e) {
                            sink.error(e);
                            return;
                        }
                    }
                    // 否则将数据发送给下一个处理程序
                    sink.next(data);
                });

        // 订阅响应流，处理每条响应
        response.subscribe(r -> {
            // 如果存在有效消息，调用回调函数处理，并累积回复内容
            if (r.message() != null) {
                try {
                    result.append(r.message().content());
                    if (onMessage != null) {
                        onMessage.accept(r.message().content(), result);
                    }

                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            }
            // 如果对话完成，完成Future
            if (Boolean.TRUE.equals(r.done())) {
                future.complete(result.toString());
            }
        }, e -> {
            // 处理异常，如果是中断异常则取消Future，否则封装异常并完成Future
            if (e instanceof InterruptedException) {
                future.cancel(true);
            } else {
                future.completeExceptionally(e);
            }
        });

        // 返回创建的Future
        return future;
    }

    /**
     * 创建一个系统消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为系统角色的消息对象。
     */
    public static OllamaApi.Message systemMessage(String message) {
        return new OllamaApi.Message(OllamaApi.Message.Role.SYSTEM, message, null, null);
    }

    /**
     * 创建一个用户消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为用户角色的消息对象。
     */
    public static OllamaApi.Message userMessage(String message) {
        return new OllamaApi.Message(OllamaApi.Message.Role.USER, message, null, null);
    }

    /**
     * 创建一个助手消息对象。
     * @param message 消息的内容。
     * @return 返回一个初始化为助手角色的消息对象。
     */
    public static OllamaApi.Message assistantMessage(String message) {
        return new OllamaApi.Message(OllamaApi.Message.Role.ASSISTANT, message, null, null);
    }

    public static OllamaApi.ChatRequest getRequest(String modelName, List<OllamaApi.Message> messages, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, boolean isStream) {
        return OllamaApi.ChatRequest.builder(ObjectUtils.defaultIfNull(modelName, OllamaModel.MISTRAL.id()))
                // 设置流式传输模式为开启
                .withStream(isStream) // streaming
                // 添加用户消息到消息列表，内容询问保加利亚的首都、国家大小及国歌
                .withMessages(messages)
                .withTools(tools)
                // 设置模型运行选项，例如温度设为0.9
                .withOptions(options)
                // 构建最终的ChatRequest对象
                .build();
    }

    @FunctionalInterface
    public interface OnMessage {
        void accept(String message, StringBuilder allReply) throws Throwable;
    }
}
