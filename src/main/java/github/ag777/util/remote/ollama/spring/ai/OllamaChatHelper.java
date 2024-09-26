package github.ag777.util.remote.ollama.spring.ai;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.spring.ai.model.AIModelReplyDeadLoopException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/4/29 下午3:26
 */
public class OllamaChatHelper {
    @Getter
    private final OllamaApi api;
    @Getter
    private final List<OllamaApi.Message> messages;
    private List<OllamaApi.ChatRequest.Tool> defaultTools;

    // 从多长回复开始执行重复判断，-1为不判断
    @Setter
    private int repeatThresholdLength;


    /**
     * 设置消息列表。
     * 这个方法会首先清空当前的消息列表，然后添加新的消息列表中的所有消息。
     *
     * @param messages 新的消息列表，类型为OllamaApi.Message的List。
     */
    public OllamaChatHelper setMessages(List<OllamaApi.Message> messages) {
        this.messages.clear(); // 清空当前的消息列表
        this.messages.addAll(messages); // 添加新的消息列表中的所有消息
        return this;
    }

    public OllamaChatHelper setTools(List<OllamaApi.ChatRequest.Tool> tools) {
        this.defaultTools = tools;
        return this;
    }


    /**
     * 清除当前聊天消息的方法。
     * 这个方法会清空当前实例中存储的所有消息。
     */
    public void clearMessage() {
        this.messages.clear();
    }

    public OllamaChatHelper() {
        this("http://127.0.0.1:11434", Collections.emptyList());
    }

    /**
     * OllamaChatHelper类的构造函数。
     *
     * @param baseUrl 用于API调用的基础URL。
     * @param messages 聊天消息的列表，这些消息将被这个助手机器人处理。
     */
    public OllamaChatHelper(String baseUrl, List<OllamaApi.Message> messages) {
        // 通过另一个构造函数初始化OllamaApi和消息列表
        this(new OllamaApi(baseUrl),
                messages
        );
    }

    public OllamaChatHelper(OllamaApi ollamaApi, List<OllamaApi.Message> messages) {
        this.api = ollamaApi;
        if (messages != null) {
            this.messages = new ArrayList<>(messages.size());
            this.messages.addAll(messages);
        } else {
            this.messages = new ArrayList<>(1);
        }
        this.repeatThresholdLength = 50;
    }


    /**
     * 向指定模型发送聊天消息并获取回复。
     *
     * @param modelName 模型名称，指定与之聊天的模型。
     * @param options 聊天选项，可包含各种配置参数。
     * @param message 发送给模型的消息内容。
     * @return 从模型收到的回复内容。
     * @throws ValidateException 如果验证失败或聊天过程中发生异常。
     */
    public String chat(String modelName, Map<String, Object> options, String message) throws ValidateException {
        return chat(modelName, options, defaultTools, message);
    }

    public String chat(String modelName, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, String message) throws ValidateException {
        addAsk2Message(message); // 添加发送的消息到消息列表
        OllamaApi.ChatRequest request = getRequest(modelName, messages, options, tools, false); // 构建聊天请求

        try {
            OllamaApi.ChatResponse response = api.chat(request); // 发送请求并获取回复
            String reply = response.message().content(); // 提取回复内容
            addReply2Message(reply); // 添加回复到消息列表
            return reply; // 返回回复内容
        } catch (Exception e) {
            // 移除最后添加的消息，因为获取回复失败
            removeLast();
            throw new ValidateException(e.getMessage(), e); // 将异常封装并抛出
        }
    }

    /**
     * 异步聊天函数，发送消息并接收回复。
     *
     * @param modelName 要对话的模型名称。
     * @param options 聊天选项映射，可包含各种对话定制选项。
     * @param message 要发送的消息内容。
     * @param onMessage 消息回调消费函数，用于处理接收到的每条消息。
     * @return CompletableFuture<String> 异步完成的未来对象，包含累计的回复内容。
     */
    public CompletableFuture<String> chatAsync(String modelName, Map<String, Object> options, String message, onMessage onMessage) {
        return chatAsync(modelName, options, defaultTools, message, onMessage);
    }

    public CompletableFuture<String> chatAsync(String modelName, Map<String, Object> options, List<OllamaApi.ChatRequest.Tool> tools, String message, onMessage onMessage) {
        // 添加发送的消息到消息列表
        addAsk2Message(message);

        // 创建一个CompletableFuture，并注册完成时的处理逻辑
        CompletableFuture<String> future = new CompletableFuture<>();
        future.whenComplete((reply, throwable) -> {
            // 如果Future被取消或有异常发生，则移除最后添加的消息
            if (future.isCancelled() || throwable != null) {
                removeLast();
            } else { // 正常完成时，添加回复到消息列表
                addReply2Message(reply);
            }
        });

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


    /**
     * 将用户询问添加到消息列表中。
     * @param ask 表示用户提出的询问内容。
     */
    private void addAsk2Message(String ask) {
        messages.add(userMessage(ask));
    }

    /**
     * 将助手回复添加到消息列表中。
     * @param reply 表示助手给出的回复内容。
     */
    private void addReply2Message(String reply) {
        messages.add(assistantMessage(reply));
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

    private void removeLast() {
        if (!messages.isEmpty()) {
            messages.removeLast();
        }
    }

    @FunctionalInterface
    public interface onMessage {
        void accept(String message, StringBuilder allReply) throws Throwable;
    }

}
