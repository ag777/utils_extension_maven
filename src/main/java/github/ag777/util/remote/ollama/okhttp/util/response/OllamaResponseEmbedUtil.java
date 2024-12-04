package github.ag777.util.remote.ollama.okhttp.util.response;

import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ollama 生成嵌入向量响应
 * 包含生成的嵌入向量和相关统计信息
 * 
 * 响应示例:
 * {
 *   "model": "llama2",                     // 使用的模型名称
 *   "embeddings": [                        // 嵌入向量列表
 *     [0.1, 0.2, 0.3, ...],               // 第一个文本的向量
 *     [0.4, 0.5, 0.6, ...]                // 第二个文本的向量（如果有）
 *   ],
 *   "total_duration": 5043500667,         // 总耗时（纳秒）
 *   "load_duration": 5025959,             // 模型加载耗时（纳秒）
 *   "prompt_eval_count": 26,              // 提示词评估的token数
 *   "prompt_eval_duration": 5025959       // 提示词评估耗时（纳秒）
 * }
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
@Getter
public class OllamaResponseEmbedUtil extends OllamaResponseBaseUtil {

    /**
     * 从 JSON 对象创建响应实例
     * 基础字段（model、duration等）由父类处理
     * 
     * @param jo JSON 对象
     */
    public OllamaResponseEmbedUtil(JsonObject jo) {
        super(jo);
    }

    /**
     * 获取第一个嵌入向量
     * 当输入是单个文本时使用此方法
     * 
     * @return 嵌入向量，如果没有则返回空列表
     * @throws JsonSyntaxException 当JSON解析出错时抛出
     */
    public List<Float> getEmbedding() throws JsonSyntaxException {
        List<List<Float>> embeddings = getEmbeddings();
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        }
        return embeddings.getFirst();
    }

    /**
     * 获取所有嵌入向量
     * 当输入是多个文本时使用此方法
     * 返回的列表中每个元素对应一个输入文本的向量
     * 
     * @return 嵌入向量列表，如果没有则返回空列表
     * @throws JsonSyntaxException 当JSON解析出错时抛出
     */
    public List<List<Float>> getEmbeddings() throws JsonSyntaxException {
        JsonArray embeddingsArray = JsonObjectUtils.getJsonArray(data, "embeddings");
        if (embeddingsArray != null) {
            List<List<Float>> embeddings = new ArrayList<>(embeddingsArray.size());
            for (JsonElement element : embeddingsArray) {
                List<Float> vector = new ArrayList<>();
                for (JsonElement value : element.getAsJsonArray()) {
                    if (value.isJsonPrimitive()) {
                        vector.add(value.getAsFloat());
                    }
                }
                embeddings.add(vector);
            }
            return embeddings;
        }
        return Collections.emptyList();
    }
}