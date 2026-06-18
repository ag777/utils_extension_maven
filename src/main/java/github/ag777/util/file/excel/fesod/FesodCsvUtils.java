package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.read.builder.CsvReaderBuilder;
import org.apache.fesod.sheet.write.builder.CsvWriterBuilder;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 基于 Apache Fesod 的 CSV 读写工具类。
 * <p>
 * Fesod 底层使用 Apache Commons CSV，通过 {@code csv()} 进入 CSV 专属 builder，
 * 可配置分隔符、引用符、换行符、null 字符串、转义符等。
 * </p>
 *
 * <pre>{@code
 * // 默认格式读取
 * List<DemoExcelData> list = FesodCsvUtils.read("demo.csv", DemoExcelData.class);
 *
 * // 自定义分隔符等（CsvConstant 提供常用常量）
 * List<DemoExcelData> tsv = FesodCsvUtils.read("demo.tsv", DemoExcelData.class,
 *         csv -> csv.delimiter(CsvConstant.TAB).nullString("N/A"));
 *
 * // 写出 CSV
 * FesodCsvUtils.write("out.csv", DemoExcelData.class, DemoExcelData.sampleList());
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see org.apache.fesod.sheet.metadata.csv.CsvConstant
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/read/csv/">Fesod CSV 读取</a>
 */
public class FesodCsvUtils {

    private FesodCsvUtils() {}

    /**
     * 按默认格式（逗号分隔）读取 CSV，映射为 POJO 列表。
     */
    public static <T> List<T> read(String csvPath, Class<T> headClass) {
        return read(csvPath, headClass, null);
    }

    /**
     * 读取 CSV，可通过 customizer 配置分隔符、引用符等。
     *
     * @param customizer CSV 参数定制器，如 {@code csv -> csv.delimiter(CsvConstant.TAB)}；null 表示默认格式
     */
    public static <T> List<T> read(String csvPath, Class<T> headClass, Consumer<CsvReaderBuilder> customizer) {
        Assert.notBlank(csvPath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        CsvReaderBuilder csvBuilder = FesodSheet.read(csvPath).head(headClass).csv();
        if (customizer != null) {
            customizer.accept(csvBuilder);
        }
        List<T> list = csvBuilder.doReadSync();
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 按默认格式写出 CSV。
     */
    public static File write(String csvPath, Class<?> headClass, Collection<?> data) {
        return write(csvPath, headClass, data, null);
    }

    /**
     * 写出 CSV，可通过 customizer 配置分隔符、引用符等。
     */
    public static File write(String csvPath, Class<?> headClass, Collection<?> data, Consumer<CsvWriterBuilder> customizer) {
        Assert.notBlank(csvPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(csvPath));
        CsvWriterBuilder csvBuilder = FesodSheet.write(outputFile, headClass).csv();
        if (customizer != null) {
            customizer.accept(csvBuilder);
        }
        csvBuilder.doWrite(data == null ? List.of() : data);
        return outputFile;
    }
}
