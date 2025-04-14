package github.ag777.util.remote.ai.okhttp.ollama.model;

import java.util.HashMap;

/**
 * Ollama大语言模型参数配置类
 * 包含了影响模型生成行为的所有可调参数
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/5 下午3:56
 */
public class OllamaOptions extends HashMap<String, Object> {

    public OllamaOptions() {
        super(30);
    }

    /**
     * 设置随机种子
     * @param seed 随机数种子，用于结果复现
     */
    public OllamaOptions seed(int seed) {
        put("seed", seed);
        return this;
    }

    /**
     * 设置保留的token数量
     * @param numKeep 保留前面的token数量，范围[1-∞]，默认值为5
     * 建议：通常保持默认值即可，除非有特殊需求
     */
    public OllamaOptions numKeep(int numKeep) {
        put("num_keep", numKeep);
        return this;
    }

    /**
     * 设置最大生成token数量
     * @param numPredict 生成的最大token数，范围[1-∞]，默认值为128
     * 建议：根据需要的回复长度调整，较短对话可设为100-200，长对话可设为500-1000
     */
    public OllamaOptions numPredict(int numPredict) {
        put("num_predict", numPredict);
        return this;
    }

    /**
     * 设置top-k采样参数
     * @param topK 从概率最高的k个token中采样，范围[1-∞]，默认值为40
     * 建议：较小的值(如20-40)产生更聚焦的输出，较大的值增加创造性
     */
    public OllamaOptions topK(int topK) {
        put("top_k", topK);
        return this;
    }

    /**
     * 设置top-p（核采样）参数
     * @param topP 累积概率阈值，范围[0-1]，默认值为0.9
     * 建议：
     * - 0.1-0.3: 非常保守，输出更确定性
     * - 0.3-0.7: 平衡创造性和连贯性
     * - 0.7-1.0: 更有创造性但可能不太可控
     */
    public OllamaOptions topP(double topP) {
        put("top_p", topP);
        return this;
    }

    /**
     * 设置最小概率阈值
     * @param minP 最小采样概率阈值，范围[0-1]，默认值为0.05
     * 建议：
     * - 较小值(0.01-0.05)允许更多样化的输出
     * - 较大值(0.1-0.2)使输出更保守
     */
    public OllamaOptions minP(double minP) {
        put("min_p", minP);
        return this;
    }

    /**
     * 设置TFS-Z得分阈值
     * @param tfsZ Tail Free Sampling参数，范围[0-∞]，默认值为1
     * 建议：
     * - 0.5-1.0: 较好的平衡性
     * - 1.0-2.0: 更保守的输出
     */
    public OllamaOptions tfsZ(double tfsZ) {
        put("tfs_z", tfsZ);
        return this;
    }

    /**
     * 设置typical sampling参数
     * @param typicalP 典型采样概率，范围[0-1]，默认值为1
     * 建议：
     * - 0.3-0.5: 更保守
     * - 0.6-0.8: 平衡性能
     * - 0.8-1.0: 更多样化
     */
    public OllamaOptions typicalP(double typicalP) {
        put("typical_p", typicalP);
        return this;
    }

    /**
     * 设置重复惩罚窗口大小
     * @param repeatLastN 检查重复的上文token数量，范围[0-∞]，默认值为64
     * 建议：设置为预期输出长度的1/4到1/3，避免重复但保持上下文连贯
     */
    public OllamaOptions repeatLastN(int repeatLastN) {
        put("repeat_last_n", repeatLastN);
        return this;
    }

    /**
     * 设置温度参数
     * @param temperature 采样温度，范围[0-2]，默认值为0.8
     * 建议：
     * - 0.1-0.4: 非常保守，适合事实性任务
     * - 0.5-0.7: 平衡性能，适合一般对话
     * - 0.8-1.0: 创造性输出，适合创意任务
     * - >1.0: 高度随机，不建议使用
     */
    public OllamaOptions temperature(double temperature) {
        put("temperature", temperature);
        return this;
    }

    /**
     * 设置重复惩罚因子
     * @param repeatPenalty 重复内容的惩罚系数，范围[1-2]，默认值为1.1
     * 建议：
     * - 1.0-1.1: 轻微避免重复
     * - 1.1-1.3: 中等程度避免重复
     * - 1.3-1.5: 强烈避免重复
     */
    public OllamaOptions repeatPenalty(double repeatPenalty) {
        put("repeat_penalty", repeatPenalty);
        return this;
    }

    /**
     * 设置存在惩罚因子
     * @param presencePenalty 对已出现token的惩罚，范围[0-2]，默认值为0
     * 建议：
     * - 0.1-0.5: 轻微鼓励新内容
     * - 0.5-1.0: 中等程度鼓励新内容
     * - 1.0-2.0: 强烈鼓励新内容
     */
    public OllamaOptions presencePenalty(double presencePenalty) {
        put("presence_penalty", presencePenalty);
        return this;
    }

    /**
     * 设置频率惩罚因子
     * @param frequencyPenalty 基于token出现频率的惩罚，范围[0-2]，默认值为0
     * 建议：
     * - 0.1-0.5: 轻微降低常见词使用
     * - 0.5-1.0: 中等程度降低常见词
     * - 1.0-2.0: 强烈降低常见词使用
     */
    public OllamaOptions frequencyPenalty(double frequencyPenalty) {
        put("frequency_penalty", frequencyPenalty);
        return this;
    }

    /**
     * 设置Mirostat算法版本
     * @param mirostat 算法版本，范围[0-2]，默认值为0
     * 建议：
     * - 0: 禁用
     * - 1: Mirostat
     * - 2: Mirostat 2.0（更激进的算法）
     */
    public OllamaOptions mirostat(int mirostat) {
        put("mirostat", mirostat);
        return this;
    }

    /**
     * 设置Mirostat目标熵
     * @param mirostatTau 目标熵值，范围[0-2]，默认值为5.0
     * 建议：
     * - 3.0-4.0: 更保守的输出
     * - 4.0-5.0: 平衡表现
     * - 5.0-6.0: 更多样化输出
     */
    public OllamaOptions mirostatTau(double mirostatTau) {
        put("mirostat_tau", mirostatTau);
        return this;
    }

    /**
     * 设置Mirostat学习率
     * @param mirostatEta Mirostat算法学习率，范围[0-1]，默认值为0.1
     * 建议：
     * - 0.05-0.1: 更稳定但适应较慢
     * - 0.1-0.2: 平衡适应速度
     * - 0.2-0.3: 快速适应但可能不稳定
     */
    public OllamaOptions mirostatEta(double mirostatEta) {
        put("mirostat_eta", mirostatEta);
        return this;
    }

    /**
     * 设置是否对换行进行惩罚
     * @param penalizeNewline true表示对换行符进行惩罚，减少不必要的换行
     * 建议：对于需要格式化输出的场景设为false，普通对话场景设为true
     */
    public OllamaOptions penalizeNewline(boolean penalizeNewline) {
        put("penalize_newline", penalizeNewline);
        return this;
    }

    /**
     * 设置停止序列
     * @param stop 生成停止的token序列数组，如["\n", "user:"]
     * 建议：根据具体应用场景设置，通常用于对话系统中分隔不同角色的发言
     */
    public OllamaOptions stop(String[] stop) {
        put("stop", stop);
        return this;
    }

    /**
     * 设置是否启用NUMA优化
     * @param numa 是否启用NUMA内存访问优化
     * 建议：多CPU系统建议启用，单CPU系统禁用
     */
    public OllamaOptions numa(boolean numa) {
        put("numa", numa);
        return this;
    }

    /**
     * 设置上下文窗口大小
     * @param numCtx 上下文窗口token数量，范围[512-∞]，默认值为2048
     * 建议：
     * - 1024: 短对话，节省内存
     * - 2048: 一般对话
     * - 4096或更高: 长文档处理，需要更多内存
     */
    public OllamaOptions numCtx(int numCtx) {
        put("num_ctx", numCtx);
        return this;
    }

    /**
     * 设置批处理大小
     * @param numBatch 批处理大小，范围[1-∞]，默认值为512
     * 建议：根据显存大小调整，较大值可提高性能但需要更多显存
     */
    public OllamaOptions numBatch(int numBatch) {
        put("num_batch", numBatch);
        return this;
    }

    /**
     * 设置GPU数量
     * @param numGpu 使用的GPU数量，范围[0-∞]，默认值为1
     * 建议：根据实际硬件配置设置，0表示仅使用CPU
     */
    public OllamaOptions numGpu(int numGpu) {
        put("num_gpu", numGpu);
        return this;
    }

    /**
     * 设置主GPU
     * @param mainGpu 主GPU索引，范围[0-numGpu-1]，默认值为0
     * 建议：多GPU系统中指定主要计算设备
     */
    public OllamaOptions mainGpu(int mainGpu) {
        put("main_gpu", mainGpu);
        return this;
    }

    /**
     * 设置是否启用低显存模式
     * @param lowVram 是否启用低显存模式
     * 建议：显存不足时启用，会牺牲一些性能
     */
    public OllamaOptions lowVram(boolean lowVram) {
        put("low_vram", lowVram);
        return this;
    }

    /**
     * 设置是否仅加载词表
     * @param vocabOnly 是否仅加载词表而不加载模型权重
     * 建议：仅在需要查看词表时启用，正常使用时禁用
     */
    public OllamaOptions vocabOnly(boolean vocabOnly) {
        put("vocab_only", vocabOnly);
        return this;
    }

    /**
     * 设置是否使用内存映射
     * @param useMmap 是否使用mmap加载模型
     * 建议：通常启用以提高加载速度，内存受限时可禁用
     */
    public OllamaOptions useMmap(boolean useMmap) {
        put("use_mmap", useMmap);
        return this;
    }

    /**
     * 设置是否锁定内存
     * @param useMlock 是否锁定内存防止交换
     * 建议：在内存充足的服务器环境建议启用，可提高性能
     */
    public OllamaOptions useMlock(boolean useMlock) {
        put("use_mlock", useMlock);
        return this;
    }

    /**
     * 设置线程数
     * @param numThread CPU线程数，范围[1-∞]，默认值为4
     * 建议：
     * - 设置为CPU核心数的1-2倍
     * - 考虑实际负载和其他应用程序需求
     */
    public OllamaOptions numThread(int numThread) {
        put("num_thread", numThread);
        return this;
    }

    /**
     * 创建一个适合问答场景的配置
     * - 较低的温度，保证答案的确定性
     * - 适中的上下文窗口
     * - 较强的重复控制
     */
    public static OllamaOptions ofQA() {
        return new OllamaOptions()
                .temperature(0.3)  // 低温度，更确定性的输出
                .topP(0.6)        // 保守的采样
                .numPredict(300)  // 适中的回答长度
                .repeatPenalty(1.2)  // 较强的重复控制
                .numCtx(2048);    // 标准上下文长度
    }

    /**
     * 创建一个适合代码生成的配置
     * - 较低的温度，保证代码的准确性
     * - 较大的上下文窗口
     * - 强力的重复控制
     */
    public static OllamaOptions ofCoding() {
        return new OllamaOptions()
                .temperature(0.2)  // 低温度，确保代码准确性
                .topP(0.4)        // 保守的采样
                .topK(30)         // 限制token选择范围
                .numPredict(800)  // 较长的生成长度，适合代码生成
                .repeatPenalty(1.3)  // 强力重复控制
                .presencePenalty(0.5)  // 鼓励使用新的token
                .numCtx(4096);    // 大上下文窗口，适合长代码
    }

    /**
     * 创建一个适合创意写作的配置
     * - 较高的温度，增加创造性
     * - 适中的上下文窗口
     * - 较弱的重复控制
     */
    public static OllamaOptions ofCreativeWriting() {
        return new OllamaOptions()
                .temperature(0.9)  // 高温度，更有创造性
                .topP(0.9)        // 更多样化的采样
                .topK(50)         // 更广的token选择范围
                .numPredict(1000)  // 较长的生成长度
                .repeatPenalty(1.1)  // 轻微的重复控制
                .presencePenalty(0.3)  // 轻微鼓励新内容
                .numCtx(3072);    // 较大上下文窗口
    }

    /**
     * 创建一个适合对话聊天的配置
     * - 中等温度，平衡确定性和创造性
     * - 标准上下文窗口
     * - 中等重复控制
     */
    public static OllamaOptions ofChat() {
        return new OllamaOptions()
                .temperature(0.7)  // 中等温度，平衡表现
                .topP(0.7)        // 平衡的采样
                .numPredict(200)  // 适中的回复长度
                .repeatPenalty(1.15)  // 中等重复控制
                .presencePenalty(0.2)  // 轻微鼓励新内容
                .numCtx(2048);     // 标准上下文长度
    }

    /**
     * 创建一个适合文本摘要的配置
     * - 低温度，保证摘要的准确性
     * - 大上下文窗口，处理长文本
     * - 强力的重复控制
     */
    public static OllamaOptions ofSummarization() {
        return new OllamaOptions()
                .temperature(0.3)  // 低温度，确保摘要准确性
                .topP(0.5)        // 保守的采样
                .numPredict(400)  // 适中的摘要长度
                .repeatPenalty(1.25)  // 较强的重复控制
                .presencePenalty(0.4)  // 鼓励使用新的表达
                .numCtx(4096);    // 大上下文窗口，适合长文本
    }

    /**
     * 创建一个适合低资源环境的配置
     * - 最小化资源占用
     * - 较小的上下文窗口
     * - 较小的批处理大小
     */
    public static OllamaOptions ofLowResource() {
        return new OllamaOptions()
                .numCtx(1024)     // 较小的上下文窗口
                .numBatch(1)      // 最小批处理大小
                .lowVram(true)    // 启用低显存模式
                .numThread(2)     // 较少的线程数
                .numPredict(100); // 较短的生成长度
    }
}
