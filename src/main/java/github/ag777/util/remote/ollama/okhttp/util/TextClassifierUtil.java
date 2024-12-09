package github.ag777.util.remote.ollama.okhttp.util;

import com.ag777.util.lang.Console;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.ag777.util.lang.exception.model.ValidateException;
import com.ag777.util.lang.model.Pair;
import github.ag777.util.remote.ollama.okhttp.OllamaApiClient;
import github.ag777.util.remote.ollama.okhttp.model.request.OllamaRequestEmbed;
import github.ag777.util.remote.ollama.okhttp.util.response.OllamaResponseEmbedUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本分类工具类
 * 基于文本嵌入向量的余弦相似度进行分类
 * 
 * 主要功能:
 * 1. 支持字典转换: 可以将相似的词转换为标准词，实现词语标准化
 * 2. 支持分类: 通过计算文本与各个类别的相似度进行分类，返回最匹配的类别
 * 3. 支持排除词: 可以设置每个类别的排除词列表，提高分类准确性
 * 
 * 使用示例:
 * <pre>
 * TextClassifierUtil classifier = new TextClassifierUtil("model-name", client)
 *     .addCategory("体育", null, List.of("体育", "跑步"), null)
 *     .addCategory("数学", "数学相关知识", null, null);
 * Pair<String, Double> result = classifier.classify("加法");
 * </pre>
 * 
 * 工作原理:
 * 1. 将输入文本转换为向量表示
 * 2. 计算与各个类别的相似度
 * 3. 根据相似度和排除规则确定最终分类
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/6
 */
public class TextClassifierUtil {
    private final String model;
    private final OllamaApiClient client;

    private final List<Pair<Keyword, Keyword>> dict;
    private final List<Category> categories;

    private double explainSimilarityThreshold = 0.8;

    /**
     * 构造函数
     * @param model Ollama模型名称
     * @param client Ollama API客户端
     */
    public TextClassifierUtil(String model, OllamaApiClient client) {
        this.model = model;
        this.client = client;
        this.dict = new ArrayList<>(10);
        this.categories = new ArrayList<>(10);
    }

    @SneakyThrows
    public static void main(String[] args) {
        String model = "qwen2.5:14b";
        TextClassifierUtil u = new TextClassifierUtil(
                model,
                new OllamaApiClient()
        )
        .addCategory(
                "体育",
                null,
                java.util.List.of("体育", "跑步", "跳高"),
                null
        ).addCategory(
                "数学",
                "数学学科内的知识，比如三角函数等",
                null,
                null
        );

        // 测试各种变体
        String[] testCases = {
                "加法",      // 标准形式
                "游泳",
                "语文"
        };
        
        for (String test : testCases) {
            Pair<String, Double> result = u.classify(test);
            Console.prettyLog("测试词: " + test);
            Console.prettyLog("分类结果: " + result.first+": "+result.second);
            Console.prettyLog("-------------------");
        }
    }

    public TextClassifierUtil withThresholdExplainSimilarity(double explainSimilarityThreshold) {
        this.explainSimilarityThreshold = explainSimilarityThreshold;
        return this;
    }

    /**
     * 对输入文本进行分类
     * 
     * @param word 待分类的文本
     * @return Pair<分类名称, 相似度分数>
     * @throws ValidateException 验证异常
     */
    public Pair<String, Double> classify(String word) throws ValidateException {
        Keyword keyword = build(word);
        
        // 1. 首先尝试字典转换
        if (!ListUtils.isEmpty(dict)) {
            String mostSimilarWord = null;
            double mostSimilarScore = -1;
            Keyword mostSimilarTarget = null;
            
            for (Pair<Keyword, Keyword> p : dict) {
                // 计算输入词与字典中第一个词的相似度
                double similarity = TextVectorUtils.cosineSimilarity(keyword.vector, p.first.vector);
                log("[字典]"+p.first.keyword+": "+similarity);
                if (similarity > mostSimilarScore) {
                    mostSimilarScore = similarity;
                    mostSimilarWord = p.first.keyword;
                    mostSimilarTarget = p.second;
                }
            }
            
            // 如果找到高相似度的匹配（阈值可调整）
            if (mostSimilarScore > explainSimilarityThreshold) {
                keyword = mostSimilarTarget;
                log("转换关键词: "+keyword);
            }
        }

        // 2. 如果字典转换未成功，继续进行分类逻辑
        if (!ListUtils.isEmpty(categories)) {
            String mostSimilarCategory = null;
            double mostSimilarCategoryScore = -1;
            for (Category category : categories) {
                double score = handleCategory(category, keyword.vector);
                if (score > mostSimilarCategoryScore) {
                    mostSimilarCategory = category.name;
                    mostSimilarCategoryScore = score;
                }
            }
            return new Pair<>(mostSimilarCategory, mostSimilarCategoryScore);
        }
        return null;
    }

    private double handleCategory(Category category, List<Float> vector) {
        double explainScore = -1;
        if (category.explain != null) {
            explainScore = TextVectorUtils.cosineSimilarity(category.explain.vector, vector);
            log("[说明]"+category.explain.keyword+": "+explainScore);
        }
        String mostSimilarExample = null;
        double mostSimilarExampleScore = -1;
        if (!ListUtils.isEmpty(category.examples)) {
            for (Keyword example : category.examples) {
                double score = TextVectorUtils.cosineSimilarity(example.vector, vector);
                log(example.keyword+": "+score);
                if (score == 1) {
                    return score;
                }
                if (score > mostSimilarExampleScore) {
                    mostSimilarExample = example.keyword;
                    mostSimilarExampleScore = score;
                }
            }
        }

        String mostSimilarExclusion = null;
        double mostSimilarExclusionScore = -1;
        if (!ListUtils.isEmpty(category.exclusions)) {
            for (Keyword exclusion : category.exclusions) {
                double score = TextVectorUtils.cosineSimilarity(exclusion.vector, vector);
                log(exclusion.keyword+": "+score);
                if (score == 1) {
                    return -score;
                }
                if (score > mostSimilarExampleScore) {
                    mostSimilarExclusion = exclusion.keyword;
                    mostSimilarExclusionScore = score;
                }
            }
        }

        if (explainScore > mostSimilarExampleScore) {
            mostSimilarExample = category.explain.keyword;
            mostSimilarExampleScore = explainScore;
        }

        double finalScore;
        if (mostSimilarExampleScore > mostSimilarExclusionScore) {
            finalScore = mostSimilarExampleScore;
        } else {
            finalScore = -mostSimilarExclusionScore;
        }
        return finalScore;
    }

    /**
     * 添加字典项
     * 
     * @param key 源词
     * @param value 目标词
     * @return this实例，支持链式调用
     * @throws ValidateException 验证异常
     */
    public TextClassifierUtil addDict(String key, String value) throws ValidateException {
        List<Keyword> vector = build(List.of(key, value));
        dict.add(new Pair<>(vector.removeFirst(), vector.removeFirst()));
        return this;
    }

    /**
     * 添加分类
     * 
     * @param name 分类名称
     * @param explain 分类说明
     * @param examples 示例词列表
     * @param exclusions 排除词列表
     * @return this实例，支持链式调用
     * @throws ValidateException 验证异常
     */
    public TextClassifierUtil addCategory(String name, String explain, List<String> examples, List<String> exclusions) throws ValidateException {
        List<String> combineList = new ArrayList<>();
        int[] sizes = new int[]{0, 0, 0};
        if (!StringUtils.isEmpty(explain)) {
            combineList.add(explain);
            sizes[0] = 1;
        }
        if (!ListUtils.isEmpty(examples)) {
            combineList.addAll(examples);
            sizes[1] = examples.size();
        }
        if (!ListUtils.isEmpty(exclusions)) {
            combineList.addAll(exclusions);
            sizes[2] = examples.size();
        }
        List<Keyword> vector = build(combineList);
        Category category = new Category().name(name);
        if (sizes[0] > 0) {
            category.explain(vector.removeFirst());
        }
        if (sizes[1] > 0) {
            category.examples(vector.subList(0, sizes[1]));
        }
        if (sizes[2] > 0) {
            category.exclusions(vector.subList(sizes[1], vector.size()));
        }
        categories.add(category);
        return this;
    }

    public Keyword build(String word) throws ValidateException {
        OllamaResponseEmbedUtil embed;
        try {
            embed = client.embed(OllamaRequestEmbed.of(model, word));
        } catch (IOException e) {
            throw new ValidateException("文本向量化接口调用失败:"+e.getMessage(), e);
        }
        try {
            return new Keyword(word, embed.getEmbedding());
        } catch (JsonSyntaxException e) {
            throw new ValidateException("文本向量化接口返回解析异常", e);
        }
    }

    public List<Keyword> build(List<String> words) throws ValidateException {
        List<Keyword> list = new ArrayList<>(words.size());
        for (String word : words) {
            list.add(build(word));
        }
        return list;
    }

    /**
     * 测试用
     * @param msg 信息
     */
    private void log(String msg) {
        System.out.println(msg);
    }


    @Data
    @Accessors(chain = true, fluent = true)
    public static class Category {
        String name;
        List<Keyword> examples;
        Keyword explain;
        List<Keyword> exclusions;
    }

    public record Keyword(String keyword, List<Float> vector) {}
}