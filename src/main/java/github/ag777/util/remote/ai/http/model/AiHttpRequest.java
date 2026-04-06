package github.ag777.util.remote.ai.http.model;

import github.ag777.util.remote.ai.openai.model.AiMessage;
import github.ag777.util.remote.ai.openai.model.request.RequestTool;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一AI请求模型。
 * 
 * <p>表示向AI模型发送的完整请求信息，包含模型名称、消息列表、工具列表、
 * 选项参数和扩展请求体。
 * 
 * <p>该类提供了便捷的链式方法来构建请求，支持：
 * <ul>
 * <li>添加系统、用户、助手消息</li>
 * <li>配置工具列表</li>
 * <li>设置温度、top_p、max_tokens等参数</li>
 * <li>添加自定义选项和扩展参数</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpRequest request = AiHttpRequest.ofModel("gpt-3.5-turbo")
 *     .system("你是一个有用的助手")
 *     .user("你好")
 *     .temperature(0.7)
 *     .maxTokens(1000);
 * </pre>
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
        messages.add(AiMessage.system(content));
        return this;
    }

    /**
     * 添加用户消息。
     * 
     * @param content 用户消息内容
     * @return 当前请求对象，支持链式调用
     */
    public AiHttpRequest user(String content) {
        messages.add(AiMessage.user(content));
        return this;
    }

    /**
     * 添加助手消息。
     * 
     * @param content 助手消息内容
     * @return 当前请求对象，支持链式调用
     */
    public AiHttpRequest assistant(String content) {
        messages.add(AiMessage.assistant(content));
        return this;
    }

    /**
     * 添加消息。
     * 
     * @param message 消息对象
     * @return 当前请求对象，支持链式调用
     */
    public AiHttpRequest message(AiMessage message) {
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
}
