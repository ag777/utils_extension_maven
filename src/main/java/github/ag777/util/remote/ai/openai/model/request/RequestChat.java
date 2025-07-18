package github.ag777.util.remote.ai.openai.model.request;

import github.ag777.util.lang.IOUtils;
import github.ag777.util.lang.security.Base64Utils;
import github.ag777.util.remote.ai.openai.model.AiMessage;
import github.ag777.util.remote.ai.openai.model.AiRoles;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 聊天请求类
 * 用于发送聊天相关的请求，支持多轮对话、工具调用和图片输入
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/04/03 下午15:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RequestChat<T extends RequestChat<T>> extends RequestBase<T> {
    private List<AiMessage> messages;
    private List<RequestTool> tools;
    private List<String> images;
    /** 停止序列，大模型遇到这个返回就停止了 */
    private List<String> stop;

    public RequestChat(String model) {
        super(model);
    }

    public static RequestChat<?> of(String model) {
        return new RequestChat<>(model);
    }

    public List<AiMessage> messages() {
        return messages;
    }

    public List<RequestTool> tools() {
        return tools;
    }

    public List<String> images() {
        return images;
    }

    public List<String> stop() {
        return stop;
    }

    public T messages(List<AiMessage> messages) {
        this.messages = messages;
        return self();
    }

    public T tools(List<RequestTool> tools) {
        this.tools = tools;
        return self();
    }

    public T images(List<String> images) {
        this.images = images;
        return self();
    }

    public T stop(List<String> stop) {
        this.stop = stop;
        return self();
    }

    /**
     * 添加一条消息
     * @param role 角色（如user, assistant）
     * @param content 消息内容
     * @return 当前请求对象
     */
    public T addMessage(String role, String content) {
        AiMessage message = AiMessage.of(role, content);
        if (this.messages == null) {
            this.messages = new ArrayList<>(5);
        }
        this.messages.add(message);
        return self();
    }

    /**
     * 添加用户消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public T user(String content) {
        return addMessage(AiRoles.USER, content);
    }

    /**
     * 添加助手消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public T assistant(String content) {
        return addMessage(AiRoles.ASSISTANT, content);
    }

    /**
     * 添加系统消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public T system(String content) {
        return addMessage(AiRoles.SYSTEM, content);
    }

    /**
     * 添加工具
     * @param tool 工具定义
     * @return 当前请求对象
     */
    public T addTool(RequestTool tool) {
        if (this.tools == null) {
            this.tools = new ArrayList<>(3);
        }
        this.tools.add(tool);
        return self();
    }

    /**
     * 添加图片
     * @param base64Image Base64编码的图片
     * @return 当前请求对象
     */
    public T addImage(String base64Image) {
        if (this.images == null) {
            this.images = new ArrayList<>(3);
        }
        this.images.add(base64Image);
        return self();
    }

    /**
     * 添加图片
     * @param imageBytes 图片字节数组
     * @return 当前请求对象
     */
    public T addImage(byte[] imageBytes) {
        return addImage(Base64Utils.encode(imageBytes));
    }

    /**
     * 添加图片
     * @param is 图片输入流
     * @return 当前请求对象
     * @throws IOException 读取图片时可能发生IO异常
     */
    public T addImage(InputStream is) throws IOException {
        return addImage(IOUtils.readBytes(is));
    }

    /**
     * 设置停止序列
     * @param texts 停止文字
     * @return 当前请求对象
     */
    public T stop(String... texts) {
        if (texts != null && texts.length>0 && !(texts.length == 1 && texts[0] == null)) {
            this.stop = Arrays.asList(texts);
        } else {
            stop = null;
        }
        return self();
    }
}
