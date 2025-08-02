package github.ag777.util.file.idm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * IDM 命令行工具类，支持通过链式调用构建并执行 Internet Download Manager (IDM) 的命令行参数。
 * 该工具类完全兼容 IDM 官方命令行参数规范，提供灵活的参数配置、CMD 字符串生成和直接执行命令功能。
 * </p>
 *
 * <p>
 * 支持的 IDM 参数包括：
 * - <code>/d URL</code>：指定下载文件的 URL。
 * - <code>/s</code>：开始任务队列。
 * - <code>/p 本地路径</code>：定义文件保存的本地路径。
 * - <code>/f 本地文件名</code>：定义文件保存的本地文件名。
 * - <code>/q</code>：下载完成后退出 IDM（仅对第一个任务有效）。
 * - <code>/h</code>：下载完成后挂起网络连接。
 * - <code>/n</code>：启用安静模式（不显示任何提示）。
 * - <code>/a</code>：将文件添加到下载队列但不立即下载。
 * </p>
 *
 * <p>
 * 示例：
 * <pre>{@code
 * String cmd = IDMCommandBuilder.builder().getDownloadCommand("http://example.com/file.zip", "C:\\Downloads", "file.zip");
 * }</pre>
 * </p>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/3 上午11:04
 */
public class IDMCommandBuilder {
    /**
     * 存储 IDM 命令行参数的键值对。
     * 键为参数名（如 "/d"），值为参数值（可为 null 表示无值参数）。
     */
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * IDM 可执行文件路径，默认为 "idman"。
     * 如果 IDM 安装路径不在系统环境变量中，需手动设置此路径。
     */
    private String idmanPath = "C:\\Program Files (x86)\\Internet Download Manager\\IDMan.exe";

    /**
     * 默认构造函数，使用默认的 IDM 可执行文件路径。
     */
    public IDMCommandBuilder() {
    }

    /**
     * 构造函数，用于指定 IDM 可执行文件路径。
     *
     * @param idmanPath IDM 可执行文件路径（例如："C:\\Program Files\\Internet Download Manager\\IDMan.exe"）
     */
    public IDMCommandBuilder(String idmanPath) {
        this.idmanPath = idmanPath;
    }

    /**
     * 静态工厂方法，用于创建一个新的 {@code IDMCommandBuilder} 实例。
     *
     * @return 新的 {@code IDMCommandBuilder} 实例
     */
    public static IDMCommandBuilder builder() {
        return new IDMCommandBuilder();
    }


    /**
     * 获取下载命令
     * @param url 下载文件的 URL
     * @param localDir 下载文件的本地路径
     * @return 生成的 CMD 命令字符串
     */
    public String getDownloadCommand(String url, String localDir) {
        return getDownloadCommand(url, localDir, null);
    }

    /**
     * 构建下载命令
     * @param url 下载文件的 URL
     * @param localDir 下载文件的本地路径
     * @param localFilename 下载文件的本地文件名
     * @return 生成的 CMD 命令字符串
     */
    public String getDownloadCommand(String url, String localDir, String localFilename) {
        url(url).path(localDir);
        if (localFilename != null) {
            filename(localFilename);
        }
        silentMode();
        return build();
    }

    /**
     * 下载文件
     * @param url 下载文件的 URL
     * @param localDir 下载文件的本地路径
     * @param onLine 下载过程中的回调
     * @return 命令执行后的退出码（0 表示成功）
     * @throws IOException 如果发生 I/O 错误
     * @throws InterruptedException 如果线程被中断
     */
    public int download(String url, String localDir, Consumer<String> onLine) throws IOException, InterruptedException {
        return download(url, localDir, null, onLine);
    }

    /**
     * 执行下载任务
     * @param url 下载文件的 URL
     * @param localDir 下载文件的本地路径
     * @param localFilename 下载文件的本地文件名
     * @param onLine 下载过程中的回调
     * @return 命令执行后的退出码（0 表示成功）
     * @throws IOException 如果发生 I/O 错误
     * @throws InterruptedException 如果线程被中断
     */
    public int download(String url, String localDir, String localFilename, Consumer<String> onLine) throws IOException, InterruptedException {
        url(url).path(localDir);
        if (localFilename != null) {
            filename(localFilename);
        }
//        silentMode();
        return execute(onLine);
    }

    /**
     * 静态工厂方法，用于创建一个新的 {@code IDMCommandBuilder} 实例。
     *
     * @param idmanPath IDM 可执行文件路径（例如："C:\\Program Files\\Internet Download Manager\\IDMan.exe"）
     * @return 新的 {@code IDMCommandBuilder} 实例
     */
    public static IDMCommandBuilder builder(String idmanPath) {
        return new IDMCommandBuilder(idmanPath);
    }

    /**
     * 获取 IDM 可执行文件路径。
     *
     * @return IDM 可执行文件路径
     */
    public String idmanPath() {
        return idmanPath;
    }

    /**
     * 设置 IDM 可执行文件路径。
     * 如果 IDM 的安装路径不在系统环境变量中，请使用此方法指定完整路径。
     *
     * @param idmanPath IDM 可执行文件路径（例如："C:\\Program Files\\Internet Download Manager\\IDMan.exe"）
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder idmanPath(String idmanPath) {
        this.idmanPath = idmanPath;
        return this;
    }

    /**
     * 设置下载文件的 URL。
     * 对应 IDM 参数：/d URL
     *
     * @param url 下载文件的 URL
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder url(String url) {
        parameters.put("/d", url);
        return this;
    }

    /**
     * 设置文件保存的本地路径。
     * 对应 IDM 参数：/p 本地路径
     *
     * @param path 本地路径（例如："C:\\Downloads"）
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder path(String path) {
        parameters.put("/p", path);
        return this;
    }

    /**
     * 设置文件保存的本地文件名。
     * 对应 IDM 参数：/f 本地文件名
     *
     * @param filename 本地文件名（例如："file.zip"）
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder filename(String filename) {
        parameters.put("/f", filename);
        return this;
    }

    /**
     * 设置下载完成后退出 IDM。
     * 对应 IDM 参数：/q
     * <p>
     * 注意：此参数仅对第一个任务有效。
     * </p>
     *
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder exitAfterDownload() {
        parameters.put("/q", null);
        return this;
    }

    /**
     * 设置下载完成后挂起网络连接。
     * 对应 IDM 参数：/h
     *
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder hangAfterDownload() {
        parameters.put("/h", null);
        return this;
    }

    /**
     * 启用安静模式（不显示任何提示）。
     * 对应 IDM 参数：/n
     *
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder silentMode() {
        parameters.put("/n", null);
        return this;
    }

    /**
     * 将文件添加到下载队列但不立即下载。
     * 对应 IDM 参数：/a
     *
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder addToQueue() {
        parameters.put("/a", null);
        return this;
    }

    /**
     * 开始任务队列中的下载任务。
     * 对应 IDM 参数：/s
     *
     * @return 当前实例，支持链式调用
     */
    public IDMCommandBuilder startQueue() {
        parameters.put("/s", null);
        return this;
    }

    /**
     * 构建完整的命令行字符串。
     * 该字符串可以直接复制到命令行中执行。
     *
     * @return 生成的 CMD 命令字符串
     */
    public String build() {
        StringBuilder cmd = new StringBuilder();
        
        // 处理 IDM 路径，如果包含空格则用引号包围
        if (idmanPath.contains(" ")) {
            cmd.append("\"").append(idmanPath).append("\"");
        } else {
            cmd.append(idmanPath);
        }
        
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            cmd.append(" ").append(entry.getKey());
            if (entry.getValue() != null) {
                // 对参数值进行转义处理
                String value = escapeParameter(entry.getValue());
                cmd.append(" ").append(value);
            }
        }
        return cmd.toString();
    }
    
    /**
     * 转义命令行参数，处理特殊字符
     * @param value 需要转义的参数值
     * @return 转义后的参数值
     */
    private String escapeParameter(String value) {
        if (value == null) {
            return null;
        }
        
        // 如果值包含空格、特殊字符或已经是引号包围的，则用引号包围
        if (value.contains(" ") || value.contains("&") || value.contains("|") || 
            value.contains("<") || value.contains(">") || value.contains("^") ||
            value.contains("(") || value.contains(")") || value.contains("%") ||
            value.contains("!") || value.contains("\"") || value.contains("'") ||
            value.startsWith("\"") && value.endsWith("\"")) {
            
            // 如果已经是引号包围的，直接返回
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value;
            }
            
            // 转义内部的引号并包围整个值
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return value;
    }

    /**
     * 执行构建的命令行并返回退出码。
     * 使用 {@link ProcessBuilder} 启动命令行进程，并输出执行结果到控制台。
     *
     * @return 命令执行后的退出码（0 表示成功）
     * @throws IOException       如果发生 I/O 错误
     * @throws InterruptedException 如果线程被中断
     */
    public int execute(Consumer<String> onLine) throws IOException, InterruptedException {
        // 构建命令参数数组，而不是单个命令字符串
        String[] commandArray = buildCommandArray();
        ProcessBuilder pb = new ProcessBuilder(commandArray);
        pb.redirectErrorStream(true); // 合并标准输出和错误流

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (onLine != null) {
                    onLine.accept(line); // 输出命令执行结果
                }
            }
        }

        return process.waitFor();
    }
    
    /**
     * 构建命令参数数组，用于 ProcessBuilder
     * @return 命令参数数组
     */
    private String[] buildCommandArray() {
        // 先计算实际需要的参数数量
        int paramCount = 1; // IDM路径
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            paramCount++; // 参数键
            if (entry.getValue() != null) {
                paramCount++; // 参数值（仅当不为null时）
            }
        }
        
        String[] commandArray = new String[paramCount];
        int index = 0;
        
        // 添加 IDM 路径
        commandArray[index++] = idmanPath;
        
        // 添加参数
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            commandArray[index++] = entry.getKey();
            if (entry.getValue() != null) {
                commandArray[index++] = entry.getValue();
            }
        }
        
        return commandArray;
    }
}