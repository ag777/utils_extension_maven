package github.ag777.util.remote.ollama.okhttp.util;

import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.okhttp.OllamaApiClient;
import github.ag777.util.remote.ollama.okhttp.model.request.OllamaRequestEmbed;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseEmbedUtil;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.*;

/**
 * 文本向量工具类
 * 提供向量相似度计算、投影计算、语义提取等功能
 * 
 * 主要功能：
 * 1. 计算文本向量的余弦相似度：将两段文本转换为向量并计算其相似度
 * 2. 支持批量计算一个文本与多个文本的相似度：高效处理多文本比较场景
 * 3. 基于Ollama API进行文本向量化：利用先进的语言模型生成文本向量表示
 * 
 * 使用示例：
 * <pre>
 * double similarity = TextVectorUtils.cosineSimilarity(
 *     client,
 *     "model-name",
 *     "第一段文本",
 *     "第二段文本"
 * );
 * </pre>
 * 
 * 技术说明：
 * - 使用余弦相似度算法计算向量相似度
 * - 相似度范围为 -1 到 1，1表示完全相同，-1表示完全相反
 * - 向量维度由使用的Ollama模型决定
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/6 下午3:18
 */
public class TextVectorUtils {

    @SneakyThrows
    public static void main(String[] args) {
        String modelName = "qwen2.5:14b";
        OllamaApiClient client = new OllamaApiClient();
        String[] texts = {
            "吃苹果"
        };
        String[][] prompts = {
            {"是水果"},
            {"吃药"},
            {"喝水"}
        };

        for (String text : texts) {
            System.out.println(text);
            // 计算当前文本与所有prompts的相似度，并存储在记录中
            Map<String, Double> matches = new HashMap<>();
            
            for (String[] prompt : prompts) {
                String promptText = prompt[0];
                List<String> extension = prompt.length > 1 ? Arrays.stream(Arrays.copyOfRange(prompt, 1, prompt.length)).toList() : null;
                try {
                    double similarity = cosineSimilarity(client, modelName, text, promptText, extension);
                    matches.put(promptText, similarity);
                } catch (Exception e) {
                    matches.put(promptText, -1.0);
                }
            }
            
            // 排序并打印结果
            matches.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> System.out.printf("%s(%.2f)%n", entry.getKey(), entry.getValue()));
        }
    }

    /**
     * 计算两个文本在特定上下文中的余弦相似度
     * 应用场景：
     * 1. 领域相关性比较：在特定领域词汇的上下文中比较两个文本
     * 2. 专业文本匹配：使用专业词汇作为上下文，比较专业文本的相似度
     * 3. 上下文敏感匹配：需要考虑特定背景信息时的文本比较
     * 
     * 特点：
     * - 可以通过words参数提供上下文信息
     * - 如果words为空，则直接比较两个文本
     * - 上下文会影响文本的向量表示，从而影响相似度计算
     * 
     * 使用示例：
     * 1. 医疗领域：
     *    words = ["医疗", "诊断", "治疗"]
     *    text1 = "头痛"
     *    text2 = "偏头痛"
     * 
     * 2. 技术领域：
     *    words = ["编程", "开发", "代码"]
     *    text1 = "Java"
     *    text2 = "Python"
     * 
     * @param client Ollama API客户端
     * @param model 使用的模型名称
     * @param text1 第一个文本
     * @param text2 第二个文本
     * @param words 上下文词列表，可以为null
     * @return 相似度分数 (范围: -1到1)
     */
    public static double cosineSimilarity(OllamaApiClient client, String model, String text1, String text2, List<String> words) throws ValidateException, IOException, JsonSyntaxException {
        if (ListUtils.isEmpty(words)) {
            return cosineSimilarity(client, model, text1, text2);
        }
        List<String> w1 = new ArrayList<>(words.size()+1);
        w1.add(text1);
        w1.addAll(words);
        OllamaResponseEmbedUtil resp = client.embed(OllamaRequestEmbed.of(model, w1));
        List<Float> v1 = resp.getEmbedding();

        List<String> w2 = new ArrayList<>(words.size()+1);
        w2.add(text2);
        w2.addAll(words);
        resp = client.embed(OllamaRequestEmbed.of(model, w2));
        List<Float> v2 = resp.getEmbedding();
        return cosineSimilarity(v1, v2);
    }

    /**
     * 在多个语义空间中提取语义
     * 可以同时处理多个语义维度（如动作、对象等）
     *
     * @param client Ollama客户端
     * @param model 模型名称
     * @param input 输入文本
     * @param semanticSpaces 语义空间映射，key为空间名称，value为二维数组：[特征词组][标准化输出]
     * @param thresholds 各语义空间的匹配阈值，key为空间名称，value为阈值
     * @return 提取结果，key为空间名称，value为提取出的标准化值
     * @throws ValidateException 验证异常
     * @throws IOException IO异常
     * @throws JsonSyntaxException JSON解析异常
     */
    public static Map<String, String> extractSemantics(
            OllamaApiClient client,
            String model,
            String input,
            Map<String, String[][]> semanticSpaces,
            Map<String, Double> thresholds) throws ValidateException, IOException, JsonSyntaxException {  // 添加每个语义空间的阈值

        List<Float> inputVector = client.embed(OllamaRequestEmbed.of(model, input)).getEmbedding();
        Map<String, String> results = new HashMap<>();

        for (Map.Entry<String, String[][]> entry : semanticSpaces.entrySet()) {
            String spaceName = entry.getKey();
            String[][] spaceData = entry.getValue();

            // 获取该语义空间的阈值，如果未指定则使用默认值
            double threshold = thresholds.getOrDefault(spaceName, 0.3);  // 默认阈值0.3

            // 转换特征词和标准输出格式
            List<List<String>> features = new ArrayList<>();
            List<String> standardOutputs = new ArrayList<>();

            for (String[] group : spaceData) {
                if (group.length < 2) continue;
                standardOutputs.add(group[0]);
                features.add(Arrays.asList(group));
            }

            // 获取所有特征词的向量
            List<List<List<Float>>> featureVectors = new ArrayList<>();
            for (List<String> feature : features) {
                List<List<Float>> vectors = client.embed(
                        OllamaRequestEmbed.of(model, feature)
                ).getEmbeddings();
                featureVectors.add(vectors);
            }

            // 提取语义，使用阈值
            String bestMatch = extractBestMatch(inputVector, featureVectors, standardOutputs, threshold);
            if (bestMatch != null) {
                results.put(spaceName, bestMatch);
            }
        }

        return results;
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
        OllamaResponseEmbedUtil resp = client.embed(OllamaRequestEmbed.of(model, text1));
        List<Float> v1 = resp.getEmbedding();
        resp = client.embed(OllamaRequestEmbed.of(model, text2));
        List<Float> v2 = resp.getEmbedding();
        return cosineSimilarity(v1, v2);
    }


    /**
     * 计算余弦相似度
     * 应用场景：
     * 1. 文本相似度匹配：比如找出最相似的文档
     * 2. 关键词匹配：检查文本是否包含特定主题
     * 3. 文本分类：将文本分类到最相似的类别
     * 
     * 特点：
     * - 值域范围：[-1, 1]，1表示方向完全一致，-1表示方向完全相反
     * - 只考虑向量的方向，不考虑大小
     * - 适合判断两个文本的相似程度
     * 
     * @param v1 向量1
     * @param v2 向量2
     * @return 相似度分数 (范围: -1到1, 1表示完全相同)
     */
    public static double cosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size() || v1.isEmpty()) {
            throw new IllegalArgumentException("向量不能为空且维度必须相同");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        // 防止除以零
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算向量在目标向量上的投影值
     * 应用场景：
     * 1. 情感分析：计算文本在正面/负面情感向量上的投影，可以判断情感强度
     * 2. 专业度评估：计算文本在专业词汇向量上的投影，评估专业程度
     * 3. 主题强度分析：评估文本对某个主题的关注程度
     * 
     * 特点：
     * - 无固定值域范围
     * - 同时考虑方向和大小
     * - 适合需要判断强度或程度的场景
     * 
     * @param v1 待投影的向量
     * @param v2 投影的目标向量
     * @return 投影值
     */
    public static double projection(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size() || v1.isEmpty()) {
            throw new IllegalArgumentException("向量不能为空且维度必须相同");
        }

        double dotProduct = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }

        // 防止除以零
        if (norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / Math.sqrt(norm2);
    }

    /**
     * 计算多个向量的平均向量
     * 应用场景：
     * 1. 词向量聚合：将多个相关词的向量聚合成一个主题向量
     * 2. 特征向量提取：从多个样本中提取平均特征
     * 3. 文本主题表示：用多个关键词的平均向量表示某个主题或领域
     * 
     * @param vectors 向量列表
     * @return 平均向量
     * @throws IllegalArgumentException 如果向量列表为空或向量维度不一致
     */
    public static List<Float> calculateMeanVector(List<List<Float>> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            throw new IllegalArgumentException("向量列表不能为空");
        }
        
        int dimension = vectors.getFirst().size();
        List<Float> meanVector = new ArrayList<>(dimension);
        
        // 初始化平均向量
        for (int i = 0; i < dimension; i++) {
            final int index = i;
            double mean = vectors.stream()
                    .mapToDouble(v -> {
                        if (v.size() != dimension) {
                            throw new IllegalArgumentException("所有向量的维度必须相同");
                        }
                        return v.get(index);
                    })
                    .average()
                    .orElse(0.0);
            meanVector.add((float) mean);
        }
        
        return meanVector;
    }

    /**
     * 从特征向量列表中提取最匹配的语义项
     * 
     * @param input 输入向量
     * @param features 特征向量列表的列表，每个元素是一组特征向量
     * @param standardOutputs 标准化输出列表，与特征向量组一一对应
     * @param threshold 匹配阈值，只有投影值超过阈值才认为是有效匹配
     * @return 最匹配的标准化输出，如果没有超过阈值的匹配则返回null
     */
    public static String extractBestMatch(
            List<Float> input,
            List<List<List<Float>>> features,
            List<String> standardOutputs,
            double threshold) {  // 添加阈值参数

        if (features.size() != standardOutputs.size()) {
            throw new IllegalArgumentException("特征词列表和标准输出列表长度必须相同");
        }

        String bestMatch = null;
        double maxProj = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < features.size(); i++) {
            List<List<Float>> feature = features.get(i);
            List<Float> meanVector = calculateMeanVector(feature);
            double proj = projection(input, meanVector);

            // 只有当投影值超过阈值时才考虑这个匹配
            if (proj > maxProj && proj >= threshold) {
                maxProj = proj;
                bestMatch = standardOutputs.get(i);
            }
        }

        return bestMatch;  // 如果没有超过阈值的匹配，返回null
    }
}
