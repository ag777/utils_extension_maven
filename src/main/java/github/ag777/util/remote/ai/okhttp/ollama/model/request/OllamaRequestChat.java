package github.ag777.util.remote.ai.okhttp.ollama.model.request;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.security.Base64Utils;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaMessage;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaRoles;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaTool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ollama聊天请求类
 * 用于发送聊天相关的请求，支持多轮对话、工具调用和图片输入
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true, fluent = true)
public class OllamaRequestChat extends OllamaRequestBase<OllamaRequestChat> {
    private List<OllamaMessage> messages;
    private List<OllamaTool> tools;
    private List<String> images;

    public OllamaRequestChat(String model) {
        super(model);
    }

    public OllamaRequestChat(String model, Map<String, Object> options) {
        super(model, options);
    }

    public static OllamaRequestChat of(String model) {
        return new OllamaRequestChat(model);
    }

    public static OllamaRequestChat of(String model, Map<String, Object> options) {
        return new OllamaRequestChat(model, options);
    }

    /**
     * 添加一条消息
     * @param role 角色（如user, assistant）
     * @param content 消息内容
     * @return 当前请求对象
     */
    public OllamaRequestChat addMessage(String role, String content) {
        OllamaMessage message = OllamaMessage.of(role, content);
        if (this.messages == null) {
            this.messages = new ArrayList<>(5);
        }
        this.messages.add(message);
        return this;
    }

    /**
     * 添加用户消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public OllamaRequestChat user(String content) {
        return addMessage(OllamaRoles.USER, content);
    }

    /**
     * 添加助手消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public OllamaRequestChat assistant(String content) {
        return addMessage(OllamaRoles.ASSISTANT, content);
    }

    /**
     * 添加系统消息
     * @param content 消息内容
     * @return 当前请求对象
     */
    public OllamaRequestChat system(String content) {
        return addMessage(OllamaRoles.SYSTEM, content);
    }

    /**
     * 添加工具
     * @param tool 工具定义
     * @return 当前请求对象
     */
    public OllamaRequestChat addTool(OllamaTool tool) {
        if (this.tools == null) {
            this.tools = new ArrayList<>(3);
        }
        this.tools.add(tool);
        return this;
    }

    /**
     * 添加图片
     * @param base64Image Base64编码的图片
     * @return 当前请求对象
     */
    public OllamaRequestChat addImage(String base64Image) {
        if (this.images == null) {
            this.images = new ArrayList<>(3);
        }
        this.images.add(base64Image);
        return this;
    }

    /**
     * 添加图片
     * @param imageBytes 图片字节数组
     * @return 当前请求对象
     */
    public OllamaRequestChat addImage(byte[] imageBytes) {
        return addImage(Base64Utils.encode(imageBytes));
    }

    /**
     * 添加图片
     * @param is 图片输入流
     * @return 当前请求对象
     * @throws IOException 读取图片时可能发生IO异常
     */
    public OllamaRequestChat addImage(InputStream is) throws IOException {
        return addImage(IOUtils.readBytes(is));
    }
}
