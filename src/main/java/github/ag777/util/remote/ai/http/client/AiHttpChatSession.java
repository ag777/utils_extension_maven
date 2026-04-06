package github.ag777.util.remote.ai.http.client;

import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;
import github.ag777.util.remote.ai.http.stream.AiHttpFuture;
import github.ag777.util.remote.ai.http.stream.AiHttpStreamHandler;
import github.ag777.util.remote.ai.openai.model.AiMessage;
import github.ag777.util.remote.ai.openai.model.request.RequestTool;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 更符合直觉的会话式辅助类。
 * 
 * <p>提供了基于会话的AI聊天接口，简化了多轮对话的管理。
 * 支持维护对话历史、系统提示、工具列表和默认参数。
 * 
 * <p>主要特性：
 * <ul>
 * <li>自动管理对话历史</li>
 * <li>支持系统提示设置</li>
 * <li>工具调用管理</li>
 * <li>默认参数配置</li>
 * <li>链式调用支持</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpChatSession session = AiHttpChatSession.of(client, "gpt-3.5-turbo")
 *     .system("你是一个有用的助手")
 *     .temperature(0.7);
 * 
 * // 发送消息
 * AiHttpResponse response = session.chat("你好");
 * 
 * // 流式对话
 * session.chatStream("介绍一下Java", new AiHttpStreamHandler() {
 *     @Override
 *     public void onContent(String delta, AiHttpChunk chunk) {
 *         System.out.print(delta);
 *     }
 * });
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
@Getter
@Accessors(chain = true, fluent = true)
public class AiHttpChatSession {
    private final AiHttpClient client;
    private String model;
    private String system;
    private final List<AiMessage> history = new ArrayList<>();
    private final List<RequestTool> tools = new ArrayList<>();
    private final AiHttpRequest defaults = AiHttpRequest.create();

    /**
     * 创建AI聊天会话。
     * 
     * @param client AI HTTP客户端
     */
    public AiHttpChatSession(AiHttpClient client) {
        this.client = client;
    }

    /**
     * 创建指定模型的AI聊天会话。
     * 
     * @param client AI HTTP客户端
     * @param model 模型名称
     * @return 配置了模型的会话对象
     */
    public static AiHttpChatSession of(AiHttpClient client, String model) {
        return new AiHttpChatSession(client).model(model);
    }

    /**
     * 设置模型名称。
     * 
     * @param model 模型名称
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession model(String model) {
        this.model = model;
        return this;
    }

    /**
     * 设置系统提示。
     * 
     * @param system 系统提示内容
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession system(String system) {
        this.system = system;
        return this;
    }

    /**
     * 添加消息到历史记录。
     * 
     * @param message 消息对象
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession add(AiMessage message) {
        history.add(message);
        return this;
    }

    /**
     * 添加用户消息到历史记录。
     * 
     * @param content 用户消息内容
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession user(String content) {
        history.add(AiMessage.user(content));
        return this;
    }

    /**
     * 添加助手消息到历史记录。
     * 
     * @param content 助手消息内容
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession assistant(String content) {
        history.add(AiMessage.assistant(content));
        return this;
    }

    /**
     * 清空历史记录。
     * 
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession clearHistory() {
        history.clear();
        return this;
    }

    /**
     * 添加工具到会话。
     * 
     * @param tool 工具定义
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession addTool(RequestTool tool) {
        tools.add(tool);
        return this;
    }

    /**
     * 设置温度参数。
     * 
     * @param value 温度值
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession temperature(Number value) {
        defaults.temperature(value);
        return this;
    }

    /**
     * 设置top_p参数。
     * 
     * @param value top_p值
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession topP(Number value) {
        defaults.topP(value);
        return this;
    }

    /**
     * 设置自定义选项参数。
     * 
     * @param key 参数键
     * @param value 参数值
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession option(String key, Object value) {
        defaults.option(key, value);
        return this;
    }

    /**
     * 同步聊天对话。
     * 
     * <p>发送用户消息并等待响应，响应会自动添加到历史记录中。
     * 
     * @param userMessage 用户消息
     * @return AI响应
     */
    public AiHttpResponse chat(String userMessage) {
        AiHttpRequest request = newRequest(userMessage, null);
        return client.chat(request);
    }

    /**
     * 异步聊天对话。
     * 
     * <p>发送用户消息并返回Future，响应会自动添加到历史记录中。
     * 
     * @param userMessage 用户消息
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(String userMessage) {
        AiHttpRequest request = newRequest(userMessage, null);
        return client.chatAsync(request);
    }

    /**
     * 异步聊天对话（带流式处理）。
     * 
     * <p>发送用户消息并返回Future，支持流式处理，响应会自动添加到历史记录中。
     * 
     * @param userMessage 用户消息
     * @param handler 流式处理器
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(String userMessage, AiHttpStreamHandler handler) {
        AiHttpRequest request = newRequest(userMessage, null);
        return client.chatAsync(request, handler);
    }

    /**
     * 流式聊天对话。
     * 
     * <p>发送用户消息并进行流式处理，响应会自动添加到历史记录中。
     * 
     * @param userMessage 用户消息
     * @param handler 流式处理器
     * @return AI响应
     */
    public AiHttpResponse chatStream(String userMessage, AiHttpStreamHandler handler) {
        AiHttpRequest request = newRequest(userMessage, null);
        return client.chatStream(request, handler);
    }

    /**
     * 同步聊天对话（带自定义请求配置）。
     * 
     * <p>发送用户消息并允许自定义请求配置，响应会自动添加到历史记录中。
     * 
     * @param userMessage 用户消息
     * @param customizer 请求自定义器
     * @return AI响应
     */
    public AiHttpResponse chat(String userMessage, Consumer<AiHttpRequest> customizer) {
        AiHttpRequest request = newRequest(userMessage, customizer);
        return client.chat(request);
    }

    /**
     * 创建新的请求对象。
     * 
     * <p>基于会话配置创建请求，包含系统提示、历史消息、工具列表和默认参数。
     * 
     * @param userMessage 用户消息
     * @param customizer 请求自定义器，可以为null
     * @return 配置完成的请求对象
     */
    private AiHttpRequest newRequest(String userMessage, Consumer<AiHttpRequest> customizer) {
        AiHttpRequest request = AiHttpRequest.ofModel(model);
        if (system != null && !system.isEmpty()) {
            request.system(system);
        }
        for (AiMessage message : history) {
            request.message(message);
        }
        if (userMessage != null) {
            request.user(userMessage);
        }
        for (RequestTool tool : tools) {
            request.addTool(tool);
        }
        request.options().putAll(defaults.options());
        request.extraBody().putAll(defaults.extraBody());
        if (customizer != null) {
            customizer.accept(request);
        }
        return request;
    }
}
