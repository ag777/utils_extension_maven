package github.ag777.util.remote.ai.http.client;

import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;
import github.ag777.util.remote.ai.http.model.AiHttpToolCall;
import github.ag777.util.remote.ai.http.openai.model.AiMessage;
import github.ag777.util.remote.ai.http.openai.model.AiMessageContentPart;
import github.ag777.util.remote.ai.http.openai.model.request.RequestTool;
import github.ag777.util.remote.ai.http.stream.AiHttpFuture;
import github.ag777.util.remote.ai.http.stream.AiHttpStreamHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
 * <li>支持文本和多模态消息</li>
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
    private List<AiMessageContentPart> pendingUserParts;

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
        clearPendingUserParts();
        history.add(AiMessage.user(content));
        return this;
    }

    /**
     * 向当前轮用户消息追加文本片段。
     *
     * @param text 文本内容
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userText(String text) {
        ensurePendingUserParts();
        pendingUserParts.add(AiMessageContentPart.text(text));
        return this;
    }

    /**
     * 添加用户多模态消息到历史记录。
     *
     * @param contentParts 用户消息内容片段
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession user(List<AiMessageContentPart> contentParts) {
        clearPendingUserParts();
        history.add(AiMessage.user(contentParts));
        return this;
    }

    /**
     * 添加用户多模态消息到历史记录。
     *
     * @param contentParts 用户消息内容片段
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userParts(List<AiMessageContentPart> contentParts) {
        return user(contentParts);
    }

    /**
     * 添加用户多模态消息到历史记录。
     *
     * @param contentParts 用户消息内容片段
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userParts(AiMessageContentPart... contentParts) {
        return userParts(Arrays.asList(contentParts));
    }

    /**
     * 向当前轮用户消息追加图片片段。
     *
     * @param imageUrl 图片URL或data URL
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userImage(String imageUrl) {
        return userImage(imageUrl, null);
    }

    /**
     * 向当前轮用户消息追加图片片段。
     *
     * @param imageUrl 图片URL或data URL
     * @param detail 图片理解细节级别，可使用 AiImageDetail
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userImage(String imageUrl, String detail) {
        ensurePendingUserParts();
        pendingUserParts.add(AiMessageContentPart.imageUrl(imageUrl, detail));
        return this;
    }

    /**
     * 向当前轮用户消息追加本地图片片段。
     *
     * @param file 本地图片文件
     * @return 当前会话对象，支持链式调用
     * @throws IOException 读取文件失败
     */
    public AiHttpChatSession userImageFile(File file) throws IOException {
        return userImageFile(file, null);
    }

    /**
     * 向当前轮用户消息追加本地图片片段。
     *
     * @param path 本地图片路径
     * @return 当前会话对象，支持链式调用
     * @throws IOException 读取文件失败
     */
    public AiHttpChatSession userImageFile(Path path) throws IOException {
        return userImageFile(path, null);
    }

    /**
     * 向当前轮用户消息追加本地图片片段。
     *
     * @param file 本地图片文件
     * @param detail 图片理解细节级别，可使用 AiImageDetail
     * @return 当前会话对象，支持链式调用
     * @throws IOException 读取文件失败
     */
    public AiHttpChatSession userImageFile(File file, String detail) throws IOException {
        ensurePendingUserParts();
        pendingUserParts.add(AiMessageContentPart.imageFile(file, detail));
        return this;
    }

    /**
     * 向当前轮用户消息追加本地图片片段。
     *
     * @param path 本地图片路径
     * @param detail 图片理解细节级别，可使用 AiImageDetail
     * @return 当前会话对象，支持链式调用
     * @throws IOException 读取文件失败
     */
    public AiHttpChatSession userImageFile(Path path, String detail) throws IOException {
        ensurePendingUserParts();
        pendingUserParts.add(AiMessageContentPart.imageFile(path, detail));
        return this;
    }

    /**
     * 清空当前轮正在构建的用户多模态消息。
     *
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession clearPendingUserParts() {
        pendingUserParts = null;
        return this;
    }

    /**
     * 添加用户图文消息到历史记录。
     *
     * @param text 文本内容
     * @param imageUrl 图片URL或data URL
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userTextAndImage(String text, String imageUrl) {
        return userTextAndImage(text, imageUrl, null);
    }

    /**
     * 添加用户图文消息到历史记录。
     *
     * @param text 文本内容
     * @param imageUrl 图片URL或data URL
     * @param detail 图片理解细节级别，可使用 AiImageDetail
     * @return 当前会话对象，支持链式调用
     */
    public AiHttpChatSession userTextAndImage(String text, String imageUrl, String detail) {
        clearPendingUserParts();
        history.add(AiMessage.user(textAndImageParts(text, imageUrl, detail)));
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
    public AiHttpChatSession assistantToolCalls(String content, List<AiHttpToolCall> toolCalls) {
        clearPendingUserParts();
        history.add(AiMessage.assistantToolCalls(content, toolCalls));
        return this;
    }

    public AiHttpChatSession assistantToolCalls(AiHttpResponse response) {
        return assistantToolCalls(response == null ? null : response.content(),
                response == null ? null : response.toolCalls());
    }

    public AiHttpChatSession toolResult(String toolCallId, String content) {
        clearPendingUserParts();
        history.add(AiMessage.tool(toolCallId, content));
        return this;
    }

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
     * <p>发送当前轮链式构建的多模态用户消息。
     *
     * @return AI响应
     */
    public AiHttpResponse chat() {
        return chat((String) null);
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
     * 同步多模态聊天对话。
     *
     * @param userMessage 用户消息内容片段
     * @return AI响应
     */
    public AiHttpResponse chat(List<AiMessageContentPart> userMessage) {
        clearPendingUserParts();
        AiHttpRequest request = newRequest(AiMessage.user(userMessage), null);
        return client.chat(request);
    }

    /**
     * 同步多模态聊天对话。
     *
     * @param userMessage 用户消息内容片段
     * @return AI响应
     */
    public AiHttpResponse chatParts(AiMessageContentPart... userMessage) {
        return chat(Arrays.asList(userMessage));
    }

    /**
     * 同步图文聊天对话。
     *
     * @param text 文本内容
     * @param imageUrl 图片URL或data URL
     * @return AI响应
     */
    public AiHttpResponse chatTextAndImage(String text, String imageUrl) {
        return chatTextAndImage(text, imageUrl, null);
    }

    /**
     * 同步图文聊天对话。
     *
     * @param text 文本内容
     * @param imageUrl 图片URL或data URL
     * @param detail 图片理解细节级别，可使用 AiImageDetail
     * @return AI响应
     */
    public AiHttpResponse chatTextAndImage(String text, String imageUrl, String detail) {
        return chat(textAndImageParts(text, imageUrl, detail));
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
     * 异步多模态聊天对话。
     *
     * @param userMessage 用户消息内容片段
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(List<AiMessageContentPart> userMessage) {
        clearPendingUserParts();
        AiHttpRequest request = newRequest(AiMessage.user(userMessage), null);
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
     * 异步多模态聊天对话（带流式处理）。
     *
     * @param userMessage 用户消息内容片段
     * @param handler 流式处理器
     * @return 可取消的Future对象
     */
    public AiHttpFuture chatAsync(List<AiMessageContentPart> userMessage, AiHttpStreamHandler handler) {
        clearPendingUserParts();
        AiHttpRequest request = newRequest(AiMessage.user(userMessage), null);
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
     * 流式多模态聊天对话。
     *
     * @param userMessage 用户消息内容片段
     * @param handler 流式处理器
     * @return AI响应
     */
    public AiHttpResponse chatStream(List<AiMessageContentPart> userMessage, AiHttpStreamHandler handler) {
        clearPendingUserParts();
        AiHttpRequest request = newRequest(AiMessage.user(userMessage), null);
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
     * 同步多模态聊天对话（带自定义请求配置）。
     *
     * @param userMessage 用户消息内容片段
     * @param customizer 请求自定义器
     * @return AI响应
     */
    public AiHttpResponse chat(List<AiMessageContentPart> userMessage, Consumer<AiHttpRequest> customizer) {
        clearPendingUserParts();
        AiHttpRequest request = newRequest(AiMessage.user(userMessage), customizer);
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
        return newRequest(buildCurrentUserMessage(userMessage), customizer);
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
    private AiHttpRequest newRequest(AiMessage userMessage, Consumer<AiHttpRequest> customizer) {
        AiHttpRequest request = AiHttpRequest.ofModel(model);
        if (system != null && !system.isEmpty()) {
            request.system(system);
        }
        for (AiMessage message : history) {
            request.message(message);
        }
        if (userMessage != null) {
            request.message(userMessage);
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

    private AiMessage buildCurrentUserMessage(String textMessage) {
        List<AiMessageContentPart> parts = takePendingUserParts();
        if (textMessage != null && !textMessage.isEmpty()) {
            if (parts == null) {
                return AiMessage.user(textMessage);
            }
            parts.add(AiMessageContentPart.text(textMessage));
        }
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return AiMessage.user(parts);
    }

    private void ensurePendingUserParts() {
        if (pendingUserParts == null) {
            pendingUserParts = new ArrayList<>();
        }
    }

    private List<AiMessageContentPart> takePendingUserParts() {
        if (pendingUserParts == null || pendingUserParts.isEmpty()) {
            pendingUserParts = null;
            return null;
        }
        List<AiMessageContentPart> parts = new ArrayList<>(pendingUserParts);
        pendingUserParts = null;
        return parts;
    }

    private static List<AiMessageContentPart> textAndImageParts(String text, String imageUrl, String detail) {
        List<AiMessageContentPart> parts = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            parts.add(AiMessageContentPart.text(text));
        }
        parts.add(AiMessageContentPart.imageUrl(imageUrl, detail));
        return parts;
    }
}
