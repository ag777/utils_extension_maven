package github.ag777.util.remote.ollama.okhttp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelfile 构建工具类
 * 用于构建 Ollama Modelfile 的内容，支持所有 Modelfile 指令。
 * 
 * Modelfile 是创建和共享模型的蓝图，主要包含以下内容：
 * 1. FROM（必需）：定义要使用的基础模型或模型文件
 * 2. PARAMETER：设置模型运行参数
 * 3. TEMPLATE：设置完整的提示模板
 * 4. SYSTEM：指定系统消息
 * 5. ADAPTER：定义要应用的 LoRA 适配器
 * 6. LICENSE：指定法律许可证
 * 7. MESSAGE：指定消息历史
 * 
 * 使用示例：
 * <pre>{@code
 * // 1. 使用现有模型
 * String modelfile = ModelfileBuilder.of("llama3.2")
 *     .parameter(Parameters.TEMPERATURE, 1)
 *     .parameter(Parameters.NUM_CTX, 4096)
 *     .system("You are Mario from super mario bros.")
 *     .build();
 * 
 * // 2. 使用 GGUF 文件
 * String modelfile = ModelfileBuilder.of("./model.gguf")
 *     .system("You are a helpful AI assistant.")
 *     .build();
 * 
 * // 3. 使用 Safetensors 模型
 * String modelfile = ModelfileBuilder.of("/path/to/model/directory")
 *     .parameter(Parameters.TEMPERATURE, 0.7)
 *     .template("{{ .System }}\nUser: {{ .Prompt }}\nAssistant: {{ .Response }}")
 *     .adapter("./path/to/adapter.safetensors")
 *     .license("MIT License")
 *     .message(MessageRoles.USER, "Hi")
 *     .message(MessageRoles.ASSISTANT, "Hello! How can I help you?")
 *     .build();
 * }</pre>
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午3:53
 */
public class ModelfileBuilder {

    /**
     * 模型参数常量
     * 包含所有可用的模型参数定义
     */
    public static class Parameters {
        /** Mirostat 采样控制，0=禁用，1=Mirostat，2=Mirostat 2.0（默认：0） */
        public static final String MIROSTAT = "mirostat";
        /** Mirostat 学习率，控制算法响应速度（默认：0.1） */
        public static final String MIROSTAT_ETA = "mirostat_eta";
        /** Mirostat tau值，控制输出的连贯性和多样性（默认：5.0） */
        public static final String MIROSTAT_TAU = "mirostat_tau";
        /** 上下文窗口大小，用于生成下一个标记（默认：2048） */
        public static final String NUM_CTX = "num_ctx";
        /** 防止重复的回看范围（默认：64，0=禁用，-1=num_ctx） */
        public static final String REPEAT_LAST_N = "repeat_last_n";
        /** 重复惩罚强度，值越高惩罚越强（默认：1.1） */
        public static final String REPEAT_PENALTY = "repeat_penalty";
        /** 温度参数，控制创造性，值越高创造性越强（默认：0.8） */
        public static final String TEMPERATURE = "temperature";
        /** 随机数种子，设置特定值可以生成相同的文本（默认：0） */
        public static final String SEED = "seed";
        /** 停止序列，遇到此模式时停止生成 */
        public static final String STOP = "stop";
        /** 尾部采样参数，用于减少不太能的标记的影响（默认：1） */
        public static final String TFS_Z = "tfs_z";
        /** 最大预测token数（默认：-1，无限生成） */
        public static final String NUM_PREDICT = "num_predict";
        /** top_k采样参数，值越高答案越多样（默认：40） */
        public static final String TOP_K = "top_k";
        /** top_p采样参数，值越高文本越多样（默认：0.9） */
        public static final String TOP_P = "top_p";
        /** min_p采样参数，确保质量和多样性的平衡（默认：0.05） */
        public static final String MIN_P = "min_p";
    }

    /**
     * 消息角色常量
     * 用于 MESSAGE 指令中指定不同角色的消息
     */
    public static class MessageRoles {
        /** 系统角色，用于提供系统消息 */
        public static final String SYSTEM = "system";
        /** 用户角色，用于示例用户可能提出的问题 */
        public static final String USER = "user";
        /** 助手角色，用于示例模型应如何回应 */
        public static final String ASSISTANT = "assistant";
    }

    /** Modelfile 内容构建器 */
    private final StringBuilder content;
    /** 参数列表 */
    private final List<String> parameters;
    /** 消息历史列表 */
    private final List<String> messages;

    /**
     * 创建一个新的构建器实例
     * 
     * @param from FROM 指令的值，可以是：
     *            1. 模型名称，如 "llama3.2"
     *            2. Safetensors 模型目录路径
     *            3. GGUF 文件路径
     */
    public ModelfileBuilder(String from) {
        this.content = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.messages = new ArrayList<>();
        from(from);
    }

    /**
     * 创建一个新的构建器实例
     * 
     * @param from FROM 指令的值，可以是：
     *            1. 模型名称，如 "llama3.2"
     *            2. Safetensors 模型目录路径
     *            3. GGUF 文件路径
     * @return 构建器实例
     */
    public static ModelfileBuilder of(String from) {
        return new ModelfileBuilder(from);
    }

    /**
     * 设置 FROM 指令（必需）
     * 支持以下几种方式：
     * 1. 使用现有模型：例如 "llama3.2"
     * 2. 使用 Safetensors 模型：指定模型目录路径
     * 3. 使用 GGUF 文件：指定 .gguf 文件路径
     * 
     * @param from FROM 指令的值（必需）
     * @return 当前实例
     */
    private ModelfileBuilder from(String from) {
        if (from == null || from.trim().isEmpty()) {
            throw new IllegalArgumentException("FROM 指令的值不能为空");
        }
        content.append("FROM ").append(from).append("\n");
        return this;
    }

    /**
     * 设置模型参数
     * 建议使用 {@link Parameters} 类中定义的常量作为参数名
     * 
     * @param name 参数名称
     * @param value 参数值
     * @return 当前实例
     */
    public ModelfileBuilder parameter(String name, Object value) {
        parameters.add(String.format("PARAMETER %s %s", name, value));
        return this;
    }

    /**
     * 设置提示模板
     * 模板可以包含以下变量：
     * - {{ .System }} - 系统消息
     * - {{ .Prompt }} - 用户提示消息
     * - {{ .Response }} - 模型响应
     * 
     * @param template 模板内容
     * @return 当前实例
     */
    public ModelfileBuilder template(String template) {
        content.append("TEMPLATE \"\"\"").append(template).append("\"\"\"\n");
        return this;
    }

    /**
     * 设置系统消息
     * 用于指定模型的行为和角色
     * 
     * @param message 系统消息内容
     * @return 当前实例
     */
    public ModelfileBuilder system(String message) {
        content.append("SYSTEM \"\"\"").append(message).append("\"\"\"\n");
        return this;
    }

    /**
     * 设置适配器
     * 支持 Safetensors 和 GGUF 格式的适配器
     * 
     * @param path 适配器路径（相对路径或绝对路径）
     * @return 当前实例
     */
    public ModelfileBuilder adapter(String path) {
        content.append("ADAPTER ").append(path).append("\n");
        return this;
    }

    /**
     * 设置许可证
     * 用于指定模型的法律许可证
     * 
     * @param license 许可证内容
     * @return 当前实例
     */
    public ModelfileBuilder license(String license) {
        content.append("LICENSE \"\"\"").append(license).append("\"\"\"\n");
        return this;
    }

    /**
     * 添加消息历史
     * 用于构建对话历史，指导模型如何回应
     * 
     * @param role 角色（system/user/assistant），建议使用 {@link MessageRoles} 中的常量
     * @param message 消息内容
     * @return 当前实例
     */
    public ModelfileBuilder message(String role, String message) {
        messages.add(String.format("MESSAGE %s %s", role, message));
        return this;
    }

    /**
     * 构建 Modelfile 内容
     * 
     * @return Modelfile 内容字符串
     */
    public String build() {
        // 添加所有参数
        if (!parameters.isEmpty()) {
            content.append("\n# Parameters\n");
            content.append(parameters.stream().collect(Collectors.joining("\n"))).append("\n");
        }

        // 添加所有消息历史
        if (!messages.isEmpty()) {
            content.append("\n# Message History\n");
            content.append(messages.stream().collect(Collectors.joining("\n"))).append("\n");
        }

        return content.toString();
    }
} 