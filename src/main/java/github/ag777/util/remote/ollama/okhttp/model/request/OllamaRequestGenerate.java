package github.ag777.util.remote.ollama.okhttp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Ollama文本生成请求类
 * 用于发送文本生成相关的请求，支持提示词和后缀设置
 * 同时也用于模型加载和卸载操作
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true, fluent = true)
public class OllamaRequestGenerate extends OllamaRequestBase<OllamaRequestGenerate> {
    private String prompt;
    private String suffix;

    public OllamaRequestGenerate(String model) {
        super(model);
    }

    public OllamaRequestGenerate(String model, Map<String, Object> options) {
        super(model, options);
    }

    /**
     * 创建生成请求实例
     * @param model 模型名称
     * @param prompt 提示词
     * @return 请求实例
     */
    public static OllamaRequestGenerate of(String model, String prompt) {
        OllamaRequestGenerate request = new OllamaRequestGenerate(model);
        return request.prompt(prompt);
    }

    /**
     * 创建带选项的生成请求实例
     * @param model 模型名称
     * @param prompt 提示词
     * @param options 选项参数
     * @return 请求实例
     */
    public static OllamaRequestGenerate of(String model, String prompt, Map<String, Object> options) {
        OllamaRequestGenerate request = new OllamaRequestGenerate(model, options);
        return request.prompt(prompt);
    }

    /**
     * 创建加载模型的请求实例
     * 如果提供空的prompt，模型将被加载到内存中
     *
     * @param model 模型名称
     * @return 请求实例
     */
    public static OllamaRequestGenerate ofLoadModel(String model) {
        return new OllamaRequestGenerate(model);
    }

    /**
     * 创建卸载模型的请求实例
     * 如果提供空的prompt并且keepAlive参数设置为0，模型将从内存中卸载
     *
     * @param model 模型名称
     * @return 请求实例
     */
    public static OllamaRequestGenerate ofUnloadModel(String model) {
        return new OllamaRequestGenerate(model)
                .keepAlive(0);
    }
}
