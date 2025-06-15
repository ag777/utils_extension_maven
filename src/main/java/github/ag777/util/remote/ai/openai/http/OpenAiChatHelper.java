package github.ag777.util.remote.ai.openai.http;

import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.http.interf.OpenaiOnMessage;
import github.ag777.util.remote.ai.openai.http.request.OpenaiRequestChat;
import github.ag777.util.remote.ai.openai.http.util.OpenaiResponseChatUtil;
import github.ag777.util.remote.ai.openai.model.AiMessage;
import github.ag777.util.remote.ai.openai.model.request.RequestTool;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ollama 聊天助手类，提供链式调用和简化的消息构建
 * @author ag777
 */
@Data
@Accessors(chain = true)
public class OpenAiChatHelper {
    protected final OpenaiApiClient client;
    private String model;
    private List<RequestTool> tools;
    private final List<AiMessage> messages;
    private Float temperature;
    private Float topP;

    public OpenAiChatHelper(OpenaiApiClient client) {
        this.client = client;
        this.messages = new ArrayList<>();
    }

    public static OpenAiChatHelper create(String host, int port, String model) {
        OpenaiApiClient client = new OpenaiApiClient();
        client.host(host);
        client.port(port);
        return new OpenAiChatHelper(client)
                .setModel(model);
    }

    // 消息构建方法
    public OpenAiChatHelper system(String content) {
        messages.add(AiMessage.system(content));
        return this;
    }

    public OpenAiChatHelper user(String content) {
        messages.add(AiMessage.user(content));
        return this;
    }

    public OpenAiChatHelper assistant(String content) {
        messages.add(AiMessage.assistant(content));
        return this;
    }


    // 清空历史消息
    public OpenAiChatHelper clearHistory() {
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
    public OpenaiResponseChatUtil chat(String userMessage) throws IOException, ValidateException, Exception {
        // 添加用户消息
        List<AiMessage> messages = new ArrayList<>(this.messages.size());
        messages.addAll(this.messages);
        messages.add(AiMessage.user(userMessage));

        // 准备请求
        OpenaiRequestChat request = OpenaiRequestChat.of(model)
                .messages(messages);
        fillOptions(request);
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
    public String chatStream(String userMessage, OpenaiOnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        // 添加用户消息
        List<AiMessage> messages = new ArrayList<>(this.messages.size());
        messages.addAll(this.messages);
        messages.add(AiMessage.user(userMessage));

        // 准备请求
        OpenaiRequestChat request = OpenaiRequestChat.of(model)
                .messages(messages);
        fillOptions(request);
        StringBuilder sb = new StringBuilder();
        // 发起流式聊天请求，并累积响应消息
        client.chatStream(request, (msg, toolCalls, res)->{
            sb.append(msg);
            onMessage.accept(msg, toolCalls, res);
        });
        return sb.toString();
    }

    private void fillOptions(OpenaiRequestChat request) {
        if (temperature != null) {
            request.temperature(temperature);
        }
        if (topP != null) {
            request.topP(topP);
        }
    }
}
