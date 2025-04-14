package github.ag777.util.remote.ai.okhttp.ollama;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.okhttp.ollama.interf.OnMessage;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaMessage;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaTool;
import github.ag777.util.remote.ai.okhttp.ollama.model.request.OllamaRequestChat;
import github.ag777.util.remote.ai.okhttp.ollama.util.response.OllamaResponseChatUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ollama 聊天助手类，提供链式调用和简化的消息构建
 * @author ag777
 */
@Data
@Accessors(chain = true)
public class OllamaChatHelper {
    protected final OllamaApiClient client;
    private String model;
    private List<OllamaTool> tools;
    private Map<String, Object> options;
    private final List<OllamaMessage> messages;

    public OllamaChatHelper(OllamaApiClient client) {
        this.client = client;
        this.messages = new ArrayList<>();
    }

    public static OllamaChatHelper create(String host, int port, String model) {
        OllamaApiClient client = new OllamaApiClient();
        client.host(host);
        client.port(port);
        return new OllamaChatHelper(client)
                .setModel(model);
    }

    // 消息构建方法
    public OllamaChatHelper system(String content) {
        messages.add(OllamaMessage.system(content));
        return this;
    }

    public OllamaChatHelper user(String content) {
        messages.add(OllamaMessage.user(content));
        return this;
    }

    public OllamaChatHelper assistant(String content) {
        messages.add(OllamaMessage.assistant(content));
        return this;
    }


    // 清空历史消息
    public OllamaChatHelper clearHistory() {
        messages.clear();
        return this;
    }

    /**
     * 执行聊天
     *
     * @param userMessage 用户消息
     * @return 聊天响应
     * @throws IOException       IO异常
     * @throws ValidateException 验证异常
     * @throws Exception         其他异常
     */
    public OllamaResponseChatUtil chat(String userMessage) throws IOException, ValidateException, Exception {
        // 添加用户消息
        List<OllamaMessage> messages = new ArrayList<>(this.messages.size());
        messages.addAll(this.messages);
        messages.add(OllamaMessage.user(userMessage));

        // 准备请求
        OllamaRequestChat request = OllamaRequestChat.of(model, options)
                .messages(messages);

        return client.chat(request);
    }

    /**
     * 使用用户消息进行聊天，并通过流式方式异步返回响应
     *
     * @param userMessage 用户输入的消息
     * @param onMessage 处理消息和工具调用的消费者
     * @return 流式返回的所有消息的字符串形式
     * @throws ValidateException 当请求参数无效时抛出
     * @throws IOException 当网络请求发生错误时抛出
     * @throws InterruptedException 当线程被中断时抛出
     */
    public String chatStream(String userMessage, OnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        // 添加用户消息
        List<OllamaMessage> messages = new ArrayList<>(this.messages.size());
        messages.addAll(this.messages);
        messages.add(OllamaMessage.user(userMessage));

        // 准备请求
        OllamaRequestChat request = OllamaRequestChat.of(model, options)
                .messages(messages);
        StringBuilder sb = new StringBuilder();
        // 发起流式聊天请求，并累积响应消息
        client.chatStream(request, (msg, tcs)->{
            sb.append(msg);
            onMessage.accept(msg, tcs);
        });
        return sb.toString();
    }
}
