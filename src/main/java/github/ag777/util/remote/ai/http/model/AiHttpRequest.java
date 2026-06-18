package github.ag777.util.remote.ai.http.model;


import github.ag777.util.remote.ai.http.openai.model.AiMessage;
import github.ag777.util.remote.ai.http.openai.model.AiMessageContentPart;
import github.ag777.util.remote.ai.http.openai.model.request.RequestTool;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;



/**

 * 统一AI请求模型。

 * 

 * <p>表示向AI模型发送的完整请求信息，包含模型名称、消息列表、工具列表、

 * 选项参数和扩展请求体。

 * 

 * <p>该类提供了便捷的链式方法来构建请求，支持：

 * <ul>

 * <li>添加系统、用户、助手消息</li>

 * <li>链式构建多模态用户消息（文本 + 多图）</li>

 * <li>配置工具列表</li>

 * <li>设置温度、top_p、max_tokens等参数</li>

 * <li>添加自定义选项和扩展参数</li>

 * </ul>

 * 

 * <p>使用示例：

 * <pre>{@code

 * AiHttpRequest request = AiHttpRequest.ofModel("llava")

 *     .userText("对比这两张图有什么区别")

 *     .userImageFile(Paths.get("a.jpg"))

 *     .userImageFile(Paths.get("b.png"));

 * }</pre>

 * 

 * @author ag777

 * @since 1.0

 */

@Data

@Accessors(chain = true, fluent = true)

public class AiHttpRequest {

    private String model;

    private final List<AiMessage> messages = new ArrayList<>();

    private List<RequestTool> tools;

    private final Map<String, Object> options = new LinkedHashMap<>();

    private final Map<String, Object> extraBody = new LinkedHashMap<>();

    private List<AiMessageContentPart> pendingUserParts;



    /**

     * 创建空的请求对象。

     * 

     * @return 新的请求对象实例

     */

    public static AiHttpRequest create() {

        return new AiHttpRequest();

    }



    /**

     * 创建指定模型的请求对象。

     * 

     * @param model 模型名称

     * @return 设置了模型名称的请求对象

     */

    public static AiHttpRequest ofModel(String model) {

        return new AiHttpRequest().model(model);

    }



    /**

     * 添加系统消息。

     * 

     * @param content 系统消息内容

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest system(String content) {

        flushPendingUserParts();

        messages.add(AiMessage.system(content));

        return this;

    }



    /**

     * 添加用户纯文本消息。

     * 

     * @param content 用户消息内容

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest user(String content) {

        flushPendingUserParts();

        messages.add(AiMessage.user(content));

        return this;

    }



    /**

     * 向当前用户多模态消息追加文本片段。

     *

     * @param text 文本内容

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userText(String text) {

        ensurePendingUserParts();

        pendingUserParts.add(AiMessageContentPart.text(text));

        return this;

    }



    /**

     * 向当前用户多模态消息追加图片片段。

     *

     * @param imageUrl 图片URL或data URL

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userImage(String imageUrl) {

        return userImage(imageUrl, null);

    }



    /**

     * 向当前用户多模态消息追加图片片段。

     *

     * @param imageUrl 图片URL或data URL

     * @param detail 图片理解细节级别，可使用 AiImageDetail

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userImage(String imageUrl, String detail) {

        ensurePendingUserParts();

        pendingUserParts.add(AiMessageContentPart.imageUrl(imageUrl, detail));

        return this;

    }



    /**

     * 向当前用户多模态消息追加本地图片片段。

     *

     * @param file 本地图片文件

     * @return 当前请求对象，支持链式调用

     * @throws IOException 读取文件失败

     */

    public AiHttpRequest userImageFile(File file) throws IOException {

        return userImageFile(file, null);

    }



    /**

     * 向当前用户多模态消息追加本地图片片段。

     *

     * @param path 本地图片路径

     * @return 当前请求对象，支持链式调用

     * @throws IOException 读取文件失败

     */

    public AiHttpRequest userImageFile(Path path) throws IOException {

        return userImageFile(path, null);

    }



    /**

     * 向当前用户多模态消息追加本地图片片段。

     *

     * @param file 本地图片文件

     * @param detail 图片理解细节级别，可使用 AiImageDetail

     * @return 当前请求对象，支持链式调用

     * @throws IOException 读取文件失败

     */

    public AiHttpRequest userImageFile(File file, String detail) throws IOException {

        ensurePendingUserParts();

        pendingUserParts.add(AiMessageContentPart.imageFile(file, detail));

        return this;

    }



    /**

     * 向当前用户多模态消息追加本地图片片段。

     *

     * @param path 本地图片路径

     * @param detail 图片理解细节级别，可使用 AiImageDetail

     * @return 当前请求对象，支持链式调用

     * @throws IOException 读取文件失败

     */

    public AiHttpRequest userImageFile(Path path, String detail) throws IOException {

        ensurePendingUserParts();

        pendingUserParts.add(AiMessageContentPart.imageFile(path, detail));

        return this;

    }



    /**

     * 提交当前正在构建的用户多模态消息。

     *

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest doneUser() {

        flushPendingUserParts();

        return this;

    }



    /**

     * 将 pending 中的用户多模态片段提交到 messages。

     */

    public void flushPendingUserParts() {

        if (pendingUserParts == null || pendingUserParts.isEmpty()) {

            pendingUserParts = null;

            return;

        }

        messages.add(AiMessage.user(new ArrayList<>(pendingUserParts)));

        pendingUserParts = null;

    }



    /**

     * 添加用户多模态消息。

     *

     * @param contentParts 用户消息内容片段

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest user(List<AiMessageContentPart> contentParts) {

        flushPendingUserParts();

        messages.add(AiMessage.user(contentParts));

        return this;

    }



    /**

     * 添加用户多模态消息。

     *

     * @param contentParts 用户消息内容片段

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userParts(List<AiMessageContentPart> contentParts) {

        return user(contentParts);

    }



    /**

     * 添加用户多模态消息。

     *

     * @param contentParts 用户消息内容片段

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userParts(AiMessageContentPart... contentParts) {

        return userParts(Arrays.asList(contentParts));

    }



    /**

     * 添加单条用户图文消息。

     *

     * @param text 文本内容

     * @param imageUrl 图片URL或data URL

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userTextAndImage(String text, String imageUrl) {

        return userTextAndImage(text, imageUrl, null);

    }



    /**

     * 添加单条用户图文消息。

     *

     * @param text 文本内容

     * @param imageUrl 图片URL或data URL

     * @param detail 图片理解细节级别，可使用 AiImageDetail

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest userTextAndImage(String text, String imageUrl, String detail) {

        flushPendingUserParts();

        List<AiMessageContentPart> parts = new ArrayList<>();

        if (text != null && !text.isEmpty()) {

            parts.add(AiMessageContentPart.text(text));

        }

        parts.add(AiMessageContentPart.imageUrl(imageUrl, detail));

        messages.add(AiMessage.user(parts));

        return this;

    }



    /**

     * 添加助手消息。

     * 

     * @param content 助手消息内容

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest assistant(String content) {

        flushPendingUserParts();

        messages.add(AiMessage.assistant(content));

        return this;

    }



    /**

     * 添加携带工具调用的助手消息。

     *

     * <p>用于把模型返回的工具调用写回对话，是工具调用闭环的关键一步。

     *

     * @param content 助手文本内容，可为null

     * @param toolCalls 模型返回的工具调用列表

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest assistantToolCalls(String content, List<AiHttpToolCall> toolCalls) {

        flushPendingUserParts();

        messages.add(AiMessage.assistantToolCalls(content, toolCalls));

        return this;

    }



    /**

     * 添加工具结果消息。

     *

     * <p>把工具执行结果回传给模型，对应 OpenAI 协议的 {@code role=tool} 消息。

     *

     * @param toolCallId 对应的工具调用ID

     * @param content 工具执行结果内容

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest toolResult(String toolCallId, String content) {

        flushPendingUserParts();

        messages.add(AiMessage.tool(toolCallId, content));

        return this;

    }



    /**

     * 添加消息。

     * 

     * @param message 消息对象

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest message(AiMessage message) {

        flushPendingUserParts();

        messages.add(message);

        return this;

    }



    /**

     * 设置消息列表。

     * 

     * @param messages 消息列表，会替换现有消息

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest messages(List<AiMessage> messages) {

        flushPendingUserParts();

        this.messages.clear();

        if (messages != null) {

            this.messages.addAll(messages);

        }

        return this;

    }



    /**

     * 添加工具。

     * 

     * @param tool 工具定义

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest addTool(RequestTool tool) {

        if (this.tools == null) {

            this.tools = new ArrayList<>();

        }

        this.tools.add(tool);

        return this;

    }



    /**

     * 设置选项参数。

     * 

     * @param key 参数键

     * @param value 参数值，如果为null则移除该参数

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest option(String key, Object value) {

        if (value == null) {

            this.options.remove(key);

        } else {

            this.options.put(key, value);

        }

        return this;

    }



    /**

     * 设置扩展请求体参数。

     * 

     * @param key 参数键

     * @param value 参数值，如果为null则移除该参数

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest extraBody(String key, Object value) {

        if (value == null) {

            this.extraBody.remove(key);

        } else {

            this.extraBody.put(key, value);

        }

        return this;

    }



    /**

     * 设置温度参数。

     * 

     * @param value 温度值，通常在0-2之间

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest temperature(Number value) {

        return option("temperature", value);

    }



    /**

     * 设置top_p参数。

     * 

     * @param value top_p值，通常在0-1之间

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest topP(Number value) {

        return option("top_p", value);

    }



    /**

     * 设置最大token数。

     * 

     * @param value 最大token数

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest maxTokens(Number value) {

        return option("max_tokens", value);

    }



    /**

     * 设置停止词。

     * 

     * @param values 停止词数组

     * @return 当前请求对象，支持链式调用

     */

    public AiHttpRequest stop(String... values) {

        return option("stop", values);

    }



    private void ensurePendingUserParts() {

        if (pendingUserParts == null) {

            pendingUserParts = new ArrayList<>();

        }

    }

}


