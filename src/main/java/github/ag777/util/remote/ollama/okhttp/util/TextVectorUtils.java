package github.ag777.util.remote.ollama.okhttp.util;

import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.okhttp.OllamaApiClient;
import github.ag777.util.remote.ollama.okhttp.model.request.OllamaRequestEmbed;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseEmbedUtil;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文本向量工具类
 * 提供文本向量化和向量相似度计算的功能
 * 
 * 主要功能:
 * 1. 计算文本向量的余弦相似度
 * 2. 支持批量计算一个文本与多个文本的相似度
 * 3. 基于Ollama API进行文本向量化
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/6 下午3:18
 */
public class TextVectorUtils {

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println(
                cosineSimilarity(
                        new OllamaApiClient(),
                        "qwen2.5:14b",
                        "英语",
                        "语文")
        );
    }

    /**
     * 计算两个文本的相似度
     * 
     * @param client Ollama API客户端
     * @param model 模型名称
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度分数 (范围: -1到1, 1表示完全相同)
     */
    public static double cosineSimilarity(OllamaApiClient client, String model, String text1, String text2) throws ValidateException, IOException, JsonSyntaxException {
        OllamaResponseEmbedUtil resp = client.embed(OllamaRequestEmbed.of(model, List.of(text1, text2)));
        List<List<Float>> embeddings = resp.getEmbeddings();
        return cosineSimilarity(embeddings.getFirst(), embeddings.get(1));
    }

    /**
     * 计算两个向量的余弦相似度
     * 
     * @param v1 向量1
     * @param v2 向量2
     * @return 相似度分数 (范围: -1到1, 1表示完全相同)
     */
    public static double cosineSimilarity(List<Float> v1, List<Float> v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
