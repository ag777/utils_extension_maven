package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.enums.WriteDirectionEnum;
import org.apache.fesod.sheet.write.builder.ExcelWriterSheetBuilder;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.apache.fesod.sheet.write.metadata.fill.FillConfig;
import org.apache.fesod.sheet.write.metadata.fill.FillWrapper;

import java.io.File;

/**
 * 基于 Apache Fesod 的模板填充工具类。
 * <p>
 * 模板语法：单变量 {@code {name}}；列表 {@code {.field}}；多列表加前缀 {@code {data1.field}}（配合 {@link FillWrapper}）。
 * </p>
 *
 * <pre>{@code
 * // 1. 单对象填充（Map 或 POJO），模板写 {name}
 * FesodFillUtils.fill("template.xlsx", "out.xlsx", Map.of("name", "报表"));
 *
 * // 2. 列表填充，模板写 {.name} {.amount}
 * FesodFillUtils.fill("template.xlsx", "out.xlsx", orderList);
 *
 * // 3. 横向填充（列表按列展开）
 * FesodFillUtils.fill("template.xlsx", "out.xlsx", orderList,
 *         FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build());
 *
 * // 4. 多列表 + 普通变量组合填充，模板写 {data1.name} {data2.name} {date}
 * FesodFillUtils.fillMulti("template.xlsx", "out.xlsx", null,
 *         new FillWrapper("data1", list1),
 *         new FillWrapper("data2", list2),
 *         Map.of("date", "2026-06-10"));
 * }</pre>
 *
 * <p>注意：列表填充处若模板下方还有其他内容，需开启 {@code FillConfig.builder().forceNewRow(true)}，
 * 但这会使整个文件载入内存，大数据量慎用。</p>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/write/fill">Fesod 模板填充</a>
 */
public class FesodFillUtils {

    private FesodFillUtils() {}

    /**
     * 基于模板填充数据并输出到新文件。
     *
     * @param data 单变量传 Map/POJO；列表填充直接传 Collection（模板用 {@code {.field}}）
     */
    public static File fill(String templatePath, String outputPath, Object data) {
        return fill(templatePath, outputPath, data, null);
    }

    /**
     * 基于模板填充数据，支持 {@link FillConfig}（横向填充、forceNewRow 等）。
     *
     * @see WriteDirectionEnum
     */
    public static File fill(String templatePath, String outputPath, Object data, FillConfig fillConfig) {
        Assert.notBlank(templatePath, "模板路径不能为空");
        Assert.notBlank(outputPath, "输出路径不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        ExcelWriterSheetBuilder sheetBuilder = FesodSheet.write(outputFile)
                .withTemplate(templatePath)
                .sheet();
        if (fillConfig == null) {
            sheetBuilder.doFill(data);
        } else {
            sheetBuilder.doFill(data, fillConfig);
        }
        return outputFile;
    }

    /**
     * 基于模板填充数据，输出到已有 {@link File}。
     */
    public static File fill(File templateFile, File outputFile, Object data) {
        Assert.notNull(templateFile, "模板文件不能为空");
        Assert.notNull(outputFile, "输出文件不能为空");
        FesodExcelUtils.prepareOutputFile(outputFile);
        FesodSheet.write(outputFile)
                .withTemplate(templateFile)
                .sheet()
                .doFill(data);
        return outputFile;
    }

    /**
     * 组合填充：一个模板中同时填充多个列表与普通变量。
     *
     * @param fillConfig 填充配置，作用于所有 part；null 表示默认
     * @param parts      每项可为 {@link FillWrapper}（带前缀的列表）、Collection（无前缀列表）或 Map/POJO（普通变量）
     */
    public static File fillMulti(String templatePath, String outputPath, FillConfig fillConfig, Object... parts) {
        Assert.notBlank(templatePath, "模板路径不能为空");
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notEmpty(parts, "填充数据不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        try (ExcelWriter excelWriter = FesodSheet.write(outputFile).withTemplate(templatePath).build()) {
            WriteSheet writeSheet = FesodSheet.writerSheet().build();
            for (Object part : parts) {
                excelWriter.fill(part, fillConfig, writeSheet);
            }
        }
        return outputFile;
    }
}
