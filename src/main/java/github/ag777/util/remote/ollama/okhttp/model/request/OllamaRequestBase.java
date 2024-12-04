package github.ag777.util.remote.ollama.okhttp.model.request;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Ollama请求基类
 * 包含所有请求的共同属性和方法
 * 使用泛型参数实现子类的链式调用
 * 
 * @param <T> 具体的请求类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午4:06
 */
@NoArgsConstructor
@Data
@Accessors(fluent = true)
public class OllamaRequestBase<T extends OllamaRequestBase<T>> {

    private String model;
    private Map<String, Object> options;
    private Boolean stream;
    @SerializedName("keep_alive")
    private Integer keepAlive;
    private Boolean raw;
    private String format;

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public OllamaRequestBase(String model) {
        this.model = model;
    }

    public OllamaRequestBase(String model, Map<String, Object> options) {
        this.model = model;
        this.options = options;
    }

    /**
     * 设置模型名称
     * @param model 模型名称
     * @return 当前请求对象
     */
    public T model(String model) {
        this.model = model;
        return self();
    }

    /**
     * 设置选项参数
     * @param options 选项参数
     * @return 当前请求对象
     */
    public T options(Map<String, Object> options) {
        this.options = options;
        return self();
    }

    /**
     * 设置是否使用流式响应
     * @param stream 是否使用流式响应
     * @return 当前请求对象
     */
    public T stream(Boolean stream) {
        this.stream = stream;
        return self();
    }

    /**
     * 设置保持连接时间
     * @param keepAlive 保持连接的时间（秒）
     * @return 当前请求对象
     */
    public T keepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
        return self();
    }

    /**
     * 设置是否使用原始模式
     * @param raw 是否使用原始模式
     * @return 当前请求对象
     */
    public T raw(Boolean raw) {
        this.raw = raw;
        return self();
    }

    /**
     * 关闭原始模式请求
     * 此方法用于将请求切换回非原始模式，这对于在特定情况下需要以非原始格式发送或接收数据时非常有用
     * @return 当前请求对象
     */
    public T rawOff() {
        this.raw = false;
        return self();
    }

    /**
     * 设置响应格式
     * @param format 响应格式
     * @return 当前请求对象
     */
    public T format(String format) {
        this.format = format;
        return self();
    }

    /**
     * 设置请求格式为JSON
     * 当format设置为json时，输出将始终是格式良好的JSON对象
     * 重要提示：同时需要指示模型以JSON格式响应
     * @return 当前请求对象
     */
    public T formatJson() {
        this.format = "json";
        return self();
    }
}
