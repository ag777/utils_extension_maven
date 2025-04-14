package github.ag777.util.remote.ai.okhttp.ollama.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * Ollama 生成嵌入向量请求参数
 * 用于从模型生成文本的嵌入向量表示
 * 
 * 请求示例:
 * {
 *   "model": "llama2",                // 模型名称
 *   "prompt": "Here is an article",   // 单个文本输入
 *   "options": {                      // 可选参数
 *     "temperature": 0.7,
 *     "seed": 42
 *   }
 * }
 * 
 * 或批量请求:
 * {
 *   "model": "llama2",
 *   "prompt": ["text1", "text2"],     // 多个文本输入
 *   "truncate": true                  // 是否截断
 * }
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true, fluent = true)
public class OllamaRequestEmbed extends OllamaRequestBase<OllamaRequestEmbed> {

    /**
     * 要生成嵌入向量的文本或文本列表
     * 可以是单个字符串或字符串列表
     * 如果是列表，将为每个文本生成一个向量
     */
    private Object input;

    /**
     * 是否截断输入以适应上下文长度
     * 如果为 false 且超出上下文长度，将返回错误
     * 默认为 true
     */
    private Boolean truncate;

    /**
     * 创建一个新的请求实例
     * 用于单个文本输入的场景
     * 
     * @param model 模型名称，如 "llama2"
     * @param input 输入文本，将被转换为嵌入向量
     */
    public OllamaRequestEmbed(String model, String input) {
        super(model);
        this.input = input;
    }

    /**
     * 创建一个新的请求实例
     * 用于多个文本输入的场景
     * 
     * @param model 模型名称，如 "llama2"
     * @param input 输入文本列表，每个文本将生成一个向量
     */
    public OllamaRequestEmbed(String model, List<String> input) {
        super(model);
        this.input = input;
    }

    /**
     * 创建一个带有选项的新请求实例
     * 
     * @param model 模型名称，如 "llama2"
     * @param input 输入文本或文本列表
     * @param options 额外的选项参数，如温度、随机种子等
     */
    public OllamaRequestEmbed(String model, Object input, Map<String, Object> options) {
        super(model, options);
        this.input = input;
    }

    /**
     * 创建一个新的请求实例（工厂方法）
     * 用于单个文本输入的场景
     * 
     * @param model 模型名称
     * @param input 输入文本
     * @return 请求实例
     */
    public static OllamaRequestEmbed of(String model, String input) {
        return new OllamaRequestEmbed(model, input);
    }

    /**
     * 创建一个带有选项的新请求实例（工厂方法）
     * 用于单个文本输入的场景
     * 
     * @param model 模型名称
     * @param input 输入文本
     * @param options 额外的选项参数
     * @return 请求实例
     */
    public static OllamaRequestEmbed of(String model, String input, Map<String, Object> options) {
        return new OllamaRequestEmbed(model, input, options);
    }

    /**
     * 创建一个新的请求实例（工厂方法）
     * 用于多个文本输入的场景
     * 
     * @param model 模型名称
     * @param input 输入文本列表
     * @return 请求实例
     */
    public static OllamaRequestEmbed of(String model, List<String> input) {
        return new OllamaRequestEmbed(model, input);
    }

    /**
     * 创建一个带有选项的新请求实例（工厂方法）
     * 用于多个文本输入的场景
     * 
     * @param model 模型名称
     * @param input 输入文本列表
     * @param options 额外的选项参数
     * @return 请求实例
     */
    public static OllamaRequestEmbed of(String model, List<String> input, Map<String, Object> options) {
        return new OllamaRequestEmbed(model, input, options);
    }

    /**
     * 创建一个带有选项的新请求实例（工厂方法）
     * 通用方法，支持单个文本或文本列表
     * 
     * @param model 模型名称
     * @param input 输入文本或文本列表
     * @param options 额外的选项参数
     * @return 请求实例
     */
    public static OllamaRequestEmbed of(String model, Object input, Map<String, Object> options) {
        return new OllamaRequestEmbed(model, input, options);
    }

    /**
     * 设置是否截断输入以适应上下文长度
     * 
     * @param truncate 如果为 false 且超出上下文长度，将返回错误
     * @return 当前实例，支持链式调用
     */
    public OllamaRequestEmbed truncate(boolean truncate) {
        this.truncate = truncate;
        return this;
    }
} 