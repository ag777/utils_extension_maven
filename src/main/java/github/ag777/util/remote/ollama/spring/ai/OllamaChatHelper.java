package github.ag777.util.remote.ollama.spring.ai;

import com.ag777.util.lang.exception.model.ValidateException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.ollama.api.OllamaApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/11/19 下午5:38
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

        try {
            String reply = OllamaUtils.chat(api, modelName, options, tools, messages);; // 提取回复内容
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
        CompletableFuture<String> future =OllamaUtils.chatAsync(api, modelName, options, tools, messages, onMessage, repeatThresholdLength);
        future.whenComplete((reply, throwable) -> {
            // 如果Future被取消或有异常发生，则移除最后添加的消息
            if (future.isCancelled() || throwable != null) {
                removeLast();
            } else { // 正常完成时，添加回复到消息列表
                addReply2Message(reply);
            }
        });

        // 返回创建的Future
        return future;
    }




    /**
     * 将用户询问添加到消息列表中。
     * @param ask 表示用户提出的询问内容。
     */
    private void addAsk2Message(String ask) {
        messages.add(OllamaUtils.userMessage(ask));
    }

    /**
     * 将助手回复添加到消息列表中。
     * @param reply 表示助手给出的回复内容。
     */
    private void addReply2Message(String reply) {
        messages.add(OllamaUtils.assistantMessage(reply));
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
