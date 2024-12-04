package github.ag777.util.remote.ollama.okhttp.util.response;

import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.google.gson.JsonObject;

/**
 * Ollama生成响应解析工具类
 * 用于解析Ollama API的文本生成响应
 * 
 * 响应示例:
 * {
 *   "model": "llama3.2",                    // 使用的模型名称
 *   "created_at": "2023-08-04T19:22:45.499127Z",  // 响应创建时间
 *   "response": "The sky is blue because it is the color of the sky.",  // 生成的文本内容
 *   "done": true,                           // 是否生成完成
 *   "context": [1, 2, 3],                   // 上下文token ID列表
 *   "total_duration": 5043500667,           // 总耗时（纳秒）
 *   "load_duration": 5025959,               // 模型加载耗时（纳秒）
 *   "prompt_eval_count": 26,                // 提示词评估的token数
 *   "prompt_eval_duration": 325953000,      // 提示词评估耗时（纳秒）
 *   "eval_count": 290,                      // 生成评估的token数
 *   "eval_duration": 4709213000             // 生成评估耗时（纳秒）
 * }
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/5 上午10:37
 */
public class OllamaResponseGenerateUtil extends OllamaResponseBaseUtil {
    /*
    构造函数
    @param jo Ollama API返回的生成响应JSON对象
     */
    public OllamaResponseGenerateUtil(JsonObject jo) {
        super(jo);
    }

    /**
     * 获取生成的文本内容
     * 从响应中提取response字段的值
     * 
     * @return 模型生成的文本内容
     * @throws JsonSyntaxException 当JSON解析出错时抛出
     */
    public String getMessage() throws JsonSyntaxException {
        return JsonObjectUtils.getStr(data, "response");
    }

}
