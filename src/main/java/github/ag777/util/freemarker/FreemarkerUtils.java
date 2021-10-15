package github.ag777.util.freemarker;

import com.ag777.util.lang.collection.MapUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 对freemarker模板引擎的二次封装
 * @Date 2021/10/15 11:47
 */
public class FreemarkerUtils {

    private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();

    public static void main(String[] args) throws IOException, TemplateException {
        process(
                new File("D:\\temp\\程序测试\\模板引擎\\模板1.txt"),
                MapUtils.of(
                    "user", "张三"
                ), new OutputStreamWriter(System.out)
        );
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
        if (!outputFile.getParentFile().exists()) {
            outputFile.mkdirs();
        }
        process(templateFile, dataModel, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile))));
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
     * @param templateFile 模板文件
     * @param encoding 配置编码
     * @param dataModel 数据
     * @param out 输出流
     * @throws IOException io异常
     * @throws TemplateException 转换异常
     */
    public static void process(File templateFile, String encoding, Object dataModel, Writer out) throws IOException, TemplateException {
        Configuration config = getConfiguration(encoding);
        // 设置模板文件路径
        config.setDirectoryForTemplateLoading(templateFile.getParentFile());
        Template template = config.getTemplate(templateFile.getName());
        template.process(dataModel, out);
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
        return config;
    }
}
