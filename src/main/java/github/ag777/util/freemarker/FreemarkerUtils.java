package github.ag777.util.freemarker;

import com.ag777.util.lang.IOUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 对freemarker模板引擎的二次封装
 * @author ag777 <837915770@vip.qq.com>
 * @version  2024/01/23 11:50
 */
public class FreemarkerUtils {
    // 默认模板文件编码
    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();

    /**
     *
     * @param templateContent 模板内容
     * @param dataModel 数据
     * @return 处理后的字符串
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static String process(String templateContent, Object dataModel) throws IOException, TemplateException {
        Writer out = getStringWriter();
        try {
            process(templateContent, dataModel, out);
            return out.toString();
        } finally {
            IOUtils.close(out);
        }
    }
    /**
     *
     * @param templateContent 模板内容
     * @param dataModel 数据
     * @param out 输出文件对应的Writer
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(String templateContent, Object dataModel, Writer out) throws IOException, TemplateException {
        try {
            Configuration config = getConfiguration(StandardCharsets.UTF_8.toString());
            Template template = new Template("", templateContent, config);
            template.process(dataModel, out);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
     *
     * @param templateFile 模板文件
     * @param dataModel 数据
     * @param outputFile 输出文件
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(File templateFile, Object dataModel, File outputFile) throws IOException, TemplateException {
        process(templateFile, dataModel, getWriter(outputFile));
    }

    /**
     *
     * @param clazz 外部类路径
     * @param basePackagePath 相对路径,如果就在classPath下，则传空字符串
     * @param templateName 模板文件名
     * @param dataModel 数据
     * @param outputFile 输出文件
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(Class<?> clazz, String basePackagePath, String templateName, Object dataModel, File outputFile) throws IOException, TemplateException {
        process(clazz, basePackagePath, templateName, dataModel, getWriter(outputFile));
    }

    /**
     *
     * @param file 输出文件
     * @return 输出文件对应的Writer
     * @throws FileNotFoundException 文件未找到
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Writer getWriter(File file) throws FileNotFoundException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    }

    /**
     *
     * @return 控制台打印的writer, 一眼用于测试
     */
    public static Writer getConsoleWriter() {
        return new OutputStreamWriter(System.out);
    }

    /**
     *
     * @return 字符串写出流，后续通过toString方法获取模板转换结果
     */
    public static Writer getStringWriter() {
        return new StringWriter();
    }

    /**
     *
     * @param templateFile 模板文件
     * @param dataModel 数据
     * @param out 输出流
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(File templateFile, Object dataModel, Writer out) throws IOException, TemplateException {
        process(templateFile, DEFAULT_ENCODING, dataModel, out);
    }

    /**
     *
     * @param clazz 外部类路径
     * @param basePackagePath 相对路径,如果就在classPath下，则传空字符串
     * @param templateName 模板文件名
     * @param dataModel 数据
     * @param out 输出流
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(Class<?> clazz, String basePackagePath, String templateName, Object dataModel, Writer out) throws IOException, TemplateException {
        process(clazz, basePackagePath, templateName, DEFAULT_ENCODING, dataModel, out);
    }

    /**
     *
     * @param templateFile 模板文件
     * @param encoding 配置编码
     * @param dataModel 数据
     * @param out 输出流
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(File templateFile, String encoding, Object dataModel, Writer out) throws IOException, TemplateException {
        try {
            Configuration config = getConfiguration(encoding);
            // 设置模板文件路径
            config.setDirectoryForTemplateLoading(templateFile.getParentFile());
            Template template = config.getTemplate(templateFile.getName());
            template.process(dataModel, out);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
     *
     * @param clazz 外部类路径
     * @param basePackagePath 相对路径,如果就在classPath下，则传空字符串
     * @param templateName 模板文件名
     * @param encoding 模板文件编码
     * @param dataModel 数据
     * @param out 输出流
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(Class<?> clazz, String basePackagePath, String templateName, String encoding, Object dataModel, Writer out) throws IOException, TemplateException {
        try {
            Configuration config = getConfiguration(encoding);
            config.setClassLoaderForTemplateLoading(clazz.getClassLoader(), basePackagePath);
            Template template = config.getTemplate(templateName);
            template.process(dataModel, out);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
     *
     * @param name 模板名称
     * @param sourceCode 模板内容
     * @return 模板
     * @throws IOException 模板格式错误 freemarker.core.ParseException
     */
    public static Template getTemplate(String name, String sourceCode) throws IOException {
        Configuration config = FreemarkerUtils.getConfiguration(StandardCharsets.UTF_8.toString());
        return new Template(name, sourceCode, config);
    }

    /**
     *
     * @param encoding 配置编码
     * @return 配置
     */
    public static Configuration getConfiguration(String encoding) {
        // 初始化Freemarker配置
        Configuration config = new Configuration(Configuration.VERSION_2_3_0);
        // 设置编码
        config.setDefaultEncoding(encoding);
        // 设置为没有千位分,避免转换数值出现"1,514"这样的数
        config.setNumberFormat("0");
        return config;
    }
}