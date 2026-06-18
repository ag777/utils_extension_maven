package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.builder.ExcelWriterBuilder;
import org.apache.fesod.sheet.write.handler.WriteHandler;
import org.apache.fesod.sheet.write.metadata.WriteSheet;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * 基于 Apache Fesod 的高级写入工具类。
 * <p>
 * 覆盖 {@link FesodWriteUtils} 之外的进阶场景：自定义 {@link WriteHandler}（样式、合并、下拉框等，
 * 工厂见 {@link FesodWriteHandlerUtils}）、动态指定/排除列、大数据量分批写入、追加写入、加密导出。
 * </p>
 *
 * <pre>{@code
 * // 1. 带样式/列宽等 WriteHandler 写入
 * FesodWriteAdvanceUtils.write("out.xlsx", DemoExcelData.class, null, data,
 *         FesodWriteHandlerUtils.simpleStyle(IndexedColors.LIGHT_BLUE, (short) 12),
 *         FesodWriteHandlerUtils.autoColumnWidth());
 *
 * // 2. 只导出指定字段
 * FesodWriteAdvanceUtils.writeIncludeColumns("out.xlsx", DemoExcelData.class, List.of("text", "date"), data);
 *
 * // 3. 大数据量分批写入同一 Sheet（如分页查库）
 * FesodWriteAdvanceUtils.writeBatch("big.xlsx", DemoExcelData.class, null, pageIterator);
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class FesodWriteAdvanceUtils {

    private FesodWriteAdvanceUtils() {}

    /**
     * 写入并注册任意 {@link WriteHandler}（样式策略、合并策略、下拉框等）。
     *
     * @see FesodWriteHandlerUtils
     */
    public static File write(String outputPath, Class<?> headClass, String sheetName, Collection<?> data, WriteHandler... handlers) {
        Assert.notEmpty(handlers, "WriteHandler不能为空");
        return doWrite(outputPath, headClass, sheetName, data, builder -> {
            for (WriteHandler handler : handlers) {
                builder.registerWriteHandler(handler);
            }
        });
    }

    /**
     * 只导出指定字段（按模型字段名），常用于"用户勾选导出列"场景。
     */
    public static File writeIncludeColumns(String outputPath, Class<?> headClass, Collection<String> includeFieldNames, Collection<?> data) {
        Assert.notEmpty(includeFieldNames, "指定列不能为空");
        return doWrite(outputPath, headClass, null, data, builder -> builder.includeColumnFieldNames(includeFieldNames));
    }

    /**
     * 排除指定字段（按模型字段名）后导出。
     */
    public static File writeExcludeColumns(String outputPath, Class<?> headClass, Collection<String> excludeFieldNames, Collection<?> data) {
        Assert.notEmpty(excludeFieldNames, "排除列不能为空");
        return doWrite(outputPath, headClass, null, data, builder -> builder.excludeColumnFieldNames(excludeFieldNames));
    }

    /**
     * 大数据量分批写入同一 Sheet：迭代器每次返回一批数据，逐批写入，避免一次性载入内存。
     * <p>典型用法：分页查库，迭代器内部翻页。</p>
     */
    public static File writeBatch(String outputPath, Class<?> headClass, String sheetName, Iterator<? extends Collection<?>> batches) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(batches, "批次迭代器不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        try (ExcelWriter excelWriter = FesodSheet.write(outputFile, headClass).build()) {
            WriteSheet writeSheet = sheetName != null
                    ? FesodSheet.writerSheet(sheetName).build()
                    : FesodSheet.writerSheet().build();
            while (batches.hasNext()) {
                excelWriter.write(FesodWriteUtils.emptyIfNull(batches.next()), writeSheet);
            }
        }
        return outputFile;
    }

    /**
     * 追加式写入：从指定行开始写，可选择不输出表头。
     *
     * @param relativeHeadRowIndex 表头（或数据）相对起始行偏移
     * @param needHead             是否写表头
     */
    public static File writeAppend(String outputPath, Class<?> headClass, String sheetName, int relativeHeadRowIndex, boolean needHead, Collection<?> data) {
        return doWrite(outputPath, headClass, sheetName, data,
                builder -> builder.relativeHeadRowIndex(relativeHeadRowIndex).needHead(needHead));
    }

    /**
     * 导出带密码保护的 Excel。
     */
    public static File writeEncrypted(String outputPath, Class<?> headClass, String password, Collection<?> data) {
        Assert.notBlank(password, "密码不能为空");
        return doWrite(outputPath, headClass, null, data, builder -> builder.password(password));
    }

    /* ======================== 内部辅助 ======================== */

    private static File doWrite(
            String outputPath,
            Class<?> headClass,
            String sheetName,
            Collection<?> data,
            Consumer<ExcelWriterBuilder> customizer) {
        Assert.notBlank(outputPath, "输出路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        File outputFile = FesodExcelUtils.prepareOutputFile(new File(outputPath));
        ExcelWriterBuilder builder = FesodSheet.write(outputFile, headClass);
        customizer.accept(builder);
        FesodWriteUtils.sheet(builder, sheetName).doWrite(FesodWriteUtils.emptyIfNull(data));
        return outputFile;
    }
}
