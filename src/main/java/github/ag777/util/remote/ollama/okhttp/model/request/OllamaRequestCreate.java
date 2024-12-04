package github.ag777.util.remote.ollama.okhttp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Ollama 创建模型请求参数
 * 用于从 Modelfile 创建一个新的模型。建议设置 modelfile 参数而不是仅设置 path。
 * 对于远程创建，必须使用 modelfile 参数。
 * 
 * 主要功能：
 * 1. 从 Modelfile 创建新模型
 * 2. 支持对非量化模型进行量化
 * 3. 支持同步和流式两种创建方式
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true, fluent = true)
public class OllamaRequestCreate extends OllamaRequestBase<OllamaRequestCreate> {

    /**
     * 量化类型常量 - 推荐使用的类型
     */
    public static class QuantizeType {
        /** 推荐的量化类型 */
        public static final String Q4_K_M = "q4_K_M";
        public static final String Q8_0 = "q8_0";

        /** 其他支持的量化类型 */
        public static final String Q2_K = "q2_K";
        public static final String Q3_K_L = "q3_K_L";
        public static final String Q3_K_M = "q3_K_M";
        public static final String Q3_K_S = "q3_K_S";
        public static final String Q4_0 = "q4_0";
        public static final String Q4_1 = "q4_1";
        public static final String Q4_K_S = "q4_K_S";
        public static final String Q5_0 = "q5_0";
        public static final String Q5_1 = "q5_1";
        public static final String Q5_K_M = "q5_K_M";
        public static final String Q5_K_S = "q5_K_S";
        public static final String Q6_K = "q6_K";
    }

    /**
     * 要创建的模型名称（必需）
     * 格式为 model:tag，例如：mario:latest, llama3:7b
     */
    private String model;

    /**
     * Modelfile 的内容（推荐）
     * 建议设置此参数而不是仅设置 path
     * 对于远程创建，此参数是必需的
     */
    private String modelfile;

    /**
     * Modelfile 的路径（可选）
     * 不推荐仅设置此参数，建议使用 modelfile 参数
     */
    private String path;

    /**
     * 量化类型（可选）
     * 用于对非量化模型(如 float16)进行量化
     * 
     * 推荐使用：
     * - {@link QuantizeType#Q4_K_M}
     * - {@link QuantizeType#Q8_0}
     * 
     * 其他支持的类型参见 {@link QuantizeType} 类中的常量定义
     */
    private String quantize;

    /**
     * 创建一个新的请求实例
     * 
     * @param model 模型名称（必需）
     */
    public OllamaRequestCreate(String model) {
        super(model);
        this.model = model;
    }

    /**
     * 创建一个带有选项的新请求实例
     * 
     * @param model 模型名称（必需）
     * @param options 额外的选项参数
     */
    public OllamaRequestCreate(String model, Map<String, Object> options) {
        super(model, options);
        this.model = model;
    }

    /**
     * 创建一个新的请求实例（工厂方法）
     * 
     * @param model 模型名称
     * @return 请求实例
     */
    public static OllamaRequestCreate of(String model) {
        return new OllamaRequestCreate(model);
    }

    /**
     * 创建一个带有选项的新请求实例（工厂方法）
     * 
     * @param model 模型名称
     * @param options 额外的选项参数
     * @return 请求实例
     */
    public static OllamaRequestCreate of(String model, Map<String, Object> options) {
        return new OllamaRequestCreate(model, options);
    }

    /**
     * 设置模型名称
     * 
     * @param model 模型名称，格式为 model:tag
     * @return 当前实例
     */
    public OllamaRequestCreate model(String model) {
        this.model = model;
        return this;
    }

    /**
     * 设置 Modelfile 内容
     * 推荐使用此方法而不是 path()
     * 
     * @param modelfile Modelfile 的内容
     * @return 当前实例
     */
    public OllamaRequestCreate modelfile(String modelfile) {
        this.modelfile = modelfile;
        return this;
    }

    /**
     * 设置 Modelfile 路径
     * 不推荐仅使用此方法，建议使用 modelfile()
     * 
     * @param path Modelfile 的路径
     * @return 当前实例
     */
    public OllamaRequestCreate path(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置量化类型
     * 用于对非量化模型进行量化处理
     * 
     * @param quantize 量化类型，推荐使用 {@link QuantizeType#Q4_K_M} 或 {@link QuantizeType#Q8_0}
     * @return 当前实例
     * @see QuantizeType
     */
    public OllamaRequestCreate quantize(String quantize) {
        this.quantize = quantize;
        return this;
    }
} 