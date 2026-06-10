package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.enums.HeaderMergeStrategy;
import org.apache.fesod.sheet.write.builder.ExcelWriterBuilder;
import org.apache.fesod.sheet.write.builder.ExcelWriterSheetBuilder;
import org.apache.fesod.sheet.write.metadata.WriteSheet;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 基于 Apache Fesod 的 Excel 写入工具类。
 * <p>
 * 对标 {@link github.ag777.util.file.excel.ExcelExportUtils}，覆盖简单写入、动态表头、
 * 多 Sheet、模板填充等常见场景，同时保留 {@link ExcelWriter} 高级用法入口。
 * </p>
 *
 * <pre>{@code
 * // 1. POJO 简单写入
 * FesodWriteUtils.write("demo.xlsx", DemoExcelData.class, DemoExcelData.sampleList());
 *
 * // 2. 动态表头写入（无 POJO）
 * List<List<String>> head = FesodExcelUtils.headFromTitles("姓名", "年龄");
 * List<List<Object>> rows = FesodExcelUtils.rows(
 *     new Object[]{"张三", 20},
 *     new Object[]{"李四", 22}
 * );
 * FesodWriteUtils.writeDynamicHead("dynamic.xlsx", head, rows);
 *
 * // 3. 多 Sheet 写入
 * FesodWriteUtils.writeMultiSheet("multi.xlsx", DemoExcelData.class, List.of(
 *     FesodSheetWriteParam.of("汇总", DemoExcelData.sampleList()),
 *     FesodSheetWriteParam.of("明细", DemoExcelData.sampleList())
 * ));
 * }</pre>
 *
 * <p>模板填充见 {@link FesodFillUtils}；样式、指定列、分批写入等高级用法见 {@link FesodWriteAdvanceUtils}。</p>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/write/simple/">Fesod 简单写入</a>
 */
public class FesodWriteUtils {

    private FesodWriteUtils() {}

    /* ======================== 简单写入 ======================== */

    /**
     * 将 POJO 列表写入 Excel 第一个 Sheet。
     */
    public static File write(String outputPath, Class<?> headClass, Collection<?> data) {
        return write(outputPath, headClass, null, data);
    }

    /**
     * 将 POJO 列表写入指定名称的 Sheet。
     */
    public static File write(String outputPath, Class<?> headClass, String sheetName, Collection<?> data) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        sheet(FesodSheet.write(outputFile, headClass), sheetName).doWrite(emptyIfNull(data));
        return outputFile;
    }

    /**
     * 写入到输出流（调用方负责关闭流）。
     */
    public static void write(OutputStream outputStream, Class<?> headClass, String sheetName, Collection<?> data) {
        Assert.notNull(outputStream, "输出流不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        sheet(FesodSheet.write(outputStream, headClass), sheetName).doWrite(emptyIfNull(data));
    }

    /**
     * 懒加载数据写入，适合数据生成耗时或数据量大的场景。
     */
    public static File writeLazy(String outputPath, Class<?> headClass, String sheetName, Supplier<Collection<?>> dataSupplier) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(dataSupplier, "数据提供者不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        sheet(FesodSheet.write(outputFile, headClass), sheetName).doWrite(dataSupplier);
        return outputFile;
    }

    /* ======================== 动态表头 ======================== */

    /**
     * 无 POJO，按动态表头写入数据。
     *
     * @param head 表头，结构为 {@code List<List<String>>}
     * @param rows 数据行，结构为 {@code List<List<Object>>}
     */
    public static File writeDynamicHead(String outputPath, List<List<String>> head, List<List<Object>> rows) {
        return writeDynamicHead(outputPath, head, rows, null, HeaderMergeStrategy.AUTO);
    }

    /**
     * 无 POJO，按动态表头写入数据，可指定 Sheet 名称与表头合并策略。
     */
    public static File writeDynamicHead(
            String outputPath,
            List<List<String>> head,
            List<List<Object>> rows,
            String sheetName,
            HeaderMergeStrategy mergeStrategy) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notEmpty(head, "表头不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        ExcelWriterBuilder writerBuilder = FesodSheet.write(outputFile)
                .head(head)
                .headerMergeStrategy(mergeStrategy == null ? HeaderMergeStrategy.AUTO : mergeStrategy);
        sheet(writerBuilder, sheetName).doWrite(emptyIfNull(rows));
        return outputFile;
    }

    /* ======================== 多 Sheet ======================== */

    /**
     * 同一 POJO 类型写入多个 Sheet，Sheet 位置与列表顺序一致。
     */
    public static <T> File writeMultiSheet(String outputPath, Class<T> headClass, List<FesodSheetWriteParam<T>> sheets) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notEmpty(sheets, "sheet参数不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        try (ExcelWriter excelWriter = FesodSheet.write(outputFile, headClass).build()) {
            for (int i = 0; i < sheets.size(); i++) {
                FesodSheetWriteParam<T> param = sheets.get(i);
                WriteSheet writeSheet = FesodSheet.writerSheet(i, param.sheetName()).build();
                excelWriter.write(emptyIfNull(param.data()), writeSheet);
            }
        }
        return outputFile;
    }

    /**
     * 通过 {@link ExcelWriter} 执行自定义多 Sheet / 追加写入等高级逻辑。
     * <p>
     * 示例：
     * <pre>{@code
     * FesodWriteUtils.writeWithWriter("out.xlsx", DemoExcelData.class, writer -> {
     *     WriteSheet sheet1 = FesodSheet.writerSheet(0, "A").build();
     *     WriteSheet sheet2 = FesodSheet.writerSheet(1, "B").build();
     *     writer.write(data1, sheet1);
     *     writer.write(data2, sheet2);
     * });
     * }</pre>
     * </p>
     */
    public static File writeWithWriter(String outputPath, Class<?> headClass, Consumer<ExcelWriter> writerConsumer) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(writerConsumer, "写入回调不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        try (ExcelWriter excelWriter = FesodSheet.write(outputFile, headClass).build()) {
            writerConsumer.accept(excelWriter);
        }
        return outputFile;
    }

    /* ======================== 内部辅助 ======================== */

    /**
     * 构建 Sheet 写入器，sheetName 为 null 时使用 Fesod 默认名称；包级共享给同包的其他写入工具类。
     */
    static ExcelWriterSheetBuilder sheet(ExcelWriterBuilder writerBuilder, String sheetName) {
        ExcelWriterSheetBuilder sheetBuilder = writerBuilder.sheet();
        if (sheetName != null) {
            sheetBuilder.sheetName(sheetName);
        }
        return sheetBuilder;
    }

    static Collection<?> emptyIfNull(Collection<?> data) {
        return data == null ? List.of() : data;
    }
}
