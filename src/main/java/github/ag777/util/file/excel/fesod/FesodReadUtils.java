package github.ag777.util.file.excel.fesod;

import github.ag777.util.file.FileUtils;
import github.ag777.util.file.excel.fesod.listener.TolerantReadListener;
import github.ag777.util.lang.collection.CollectionAndMapUtils;
import github.ag777.util.lang.exception.Assert;
import github.ag777.util.lang.type.impl.StringInt;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.enums.ReadDefaultReturnEnum;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.read.builder.ExcelReaderBuilder;
import org.apache.fesod.sheet.read.builder.ExcelReaderSheetBuilder;
import org.apache.fesod.sheet.read.listener.PageReadListener;
import org.apache.fesod.sheet.read.listener.ReadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 基于 Apache Fesod 的 Excel 读取工具类。
 * <p>
 * 所有方法均为同步阻塞读取。{@code read*} 系列一次性返回完整结果；
 * {@link #readEach}、{@link #readBatch} 为流式（监听器）回调方式，
 * 边解析边消费，内存占用低，适合大文件，但方法返回时同样意味着解析已完成。
 * Sheet 定位使用 {@link StringInt}，传入 {@link StringInt#of(Integer)} 为下标，
 * {@link StringInt#of(String)} 为名称；省略时默认第一个 Sheet。
 * </p>
 *
 * <pre>{@code
 * // POJO 读取
 * List<DemoExcelData> list = FesodReadUtils.read("demo.xlsx", DemoExcelData.class);
 * List<DemoExcelData> sheet2 = FesodReadUtils.read("demo.xlsx", DemoExcelData.class, StringInt.of(1));
 *
 * // 无模型：二维字符串列表
 * List<List<String>> rows = FesodReadUtils.readRows("demo.xlsx");
 *
 * // 无模型：按指定标题映射为 Map
 * List<Map<String, Object>> maps = FesodReadUtils.readMap(
 *         "demo.xlsx", new String[]{"姓名", "年龄"}, true);
 *
 * // 大文件流式逐行
 * FesodReadUtils.readEach("big.xlsx", DemoExcelData.class, row -> save(row));
 *
 * // 容错读取：解析失败的行跳过并回调
 * FesodReadUtils.readEach("big.xlsx", DemoExcelData.class, null,
 *         row -> save(row), (rowIndex, e) -> log.warn("第{}行失败", rowIndex + 1, e));
 *
 * // 仅读表头（模板校验）
 * List<String> heads = FesodReadUtils.readHead("upload.xlsx");
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/read/simple/">Fesod 简单读取</a>
 */
public class FesodReadUtils {

    /** 默认分批大小 */
    public static final int DEFAULT_BATCH_SIZE = 500;

    private FesodReadUtils() {}

    /* ======================== POJO 读取 ======================== */

    /**
     * 读取第一个 Sheet，映射为 POJO 列表。
     * <p>数据量较小时使用；大文件请改用 {@link #readEach} 或 {@link #readBatch}。</p>
     */
    public static <T> List<T> read(String filePath, Class<T> headClass) {
        return read(filePath, headClass, null);
    }

    /**
     * 读取指定 Sheet，映射为 POJO 列表。
     *
     * @param sheet {@link StringInt#of(Integer)} 为下标，{@link StringInt#of(String)} 为名称；null 表示第一个 Sheet
     */
    public static <T> List<T> read(String filePath, Class<T> headClass, StringInt sheet) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        List<T> list = sheet(FesodSheet.read(filePath).head(headClass), sheet).doReadSync();
        return emptyIfNull(list);
    }

    /**
     * 从输入流读取第一个 Sheet。
     */
    public static <T> List<T> read(InputStream inputStream, Class<T> headClass) {
        return read(inputStream, headClass, null);
    }

    /**
     * 从输入流读取指定 Sheet。
     *
     * @param sheet {@link StringInt#of(Integer)} 为下标，{@link StringInt#of(String)} 为名称；null 表示第一个 Sheet
     */
    public static <T> List<T> read(InputStream inputStream, Class<T> headClass, StringInt sheet) {
        Assert.notNull(inputStream, "输入流不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        List<T> list = sheet(FesodSheet.read(inputStream).head(headClass), sheet).doReadSync();
        return emptyIfNull(list);
    }

    /**
     * 读取带密码保护的 Excel。
     */
    public static <T> List<T> readEncrypted(String filePath, Class<T> headClass, StringInt sheet, String password) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notBlank(password, "密码不能为空");
        List<T> list = sheet(
                FesodSheet.read(filePath).head(headClass).password(password),
                sheet
        ).doReadSync();
        return emptyIfNull(list);
    }

    /**
     * 读取所有 Sheet，返回合并后的 POJO 列表。
     */
    public static <T> List<T> readAllSheets(String filePath, Class<T> headClass) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        List<T> list = FesodSheet.read(filePath).head(headClass).doReadAllSync();
        return emptyIfNull(list);
    }

    /**
     * 通过 classpath 或文件系统路径读取第一个 Sheet。
     */
    public static <T> List<T> readFromResource(String resourcePath, Class<T> headClass) throws IOException {
        Assert.notBlank(resourcePath, "资源路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        try (InputStream inputStream = FileUtils.getInputStream(resourcePath)) {
            return read(inputStream, headClass);
        }
    }

    /* ======================== 表头读取 ======================== */

    /**
     * 仅读取第一个 Sheet 的表头行（按列顺序），常用于校验上传文件的模板是否正确。
     */
    public static List<String> readHead(String filePath) {
        return readHead(filePath, null);
    }

    /**
     * 仅读取指定 Sheet 的表头行。
     * <p>通过 {@link ReadListener#invokeHead} 取得表头后立即停止解析，不读取数据行。</p>
     */
    public static List<String> readHead(String filePath, StringInt sheet) {
        Assert.notBlank(filePath, "文件路径不能为空");
        List<String> heads = new ArrayList<>();
        ReadListener<Object> listener = new ReadListener<>() {
            @Override
            public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                int maxIndex = headMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
                for (int i = 0; i <= maxIndex; i++) {
                    ReadCellData<?> cell = headMap.get(i);
                    heads.add(cell == null ? null : cell.getStringValue());
                }
            }

            @Override
            public void invoke(Object data, AnalysisContext context) {
            }

            @Override
            public boolean hasNext(AnalysisContext context) {
                // 表头已取得，停止读取数据行
                return false;
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };
        sheet(FesodSheet.read(filePath).registerReadListener(listener), sheet).doRead();
        return heads;
    }

    /* ======================== 无模型读取 ======================== */

    /**
     * 无 POJO，读取为二维字符串列表，每个内层 List 代表一行单元格（按列索引顺序）。
     * <p>空行以空 List 占位，保证返回结果与 Excel 行号对齐。</p>
     */
    public static List<List<String>> readRows(String filePath) {
        return readRows(filePath, null);
    }

    /**
     * 无 POJO，读取指定 Sheet 为二维字符串列表。
     */
    public static List<List<String>> readRows(String filePath, StringInt sheet) {
        Assert.notBlank(filePath, "文件路径不能为空");
        List<Map<Integer, String>> rawRows = readRawStringRows(filePath, sheet, 0);
        return toStringRows(rawRows);
    }

    /**
     * 无 POJO，按指定标题将每行映射为 {@code Map<String, Object>}，全空行会被丢弃。
     * <p>值类型由 Fesod 自动推断，可能为 {@code String}、{@code BigDecimal}、{@code Boolean}、{@code LocalDateTime} 等。</p>
     *
     * @param titles          列标题，顺序对应 Excel 列索引
     * @param ignoreFirstRow  是否跳过第一行（通常为表头行）
     */
    public static List<Map<String, Object>> readMap(String filePath, String[] titles, boolean ignoreFirstRow) {
        return readMap(filePath, titles, ignoreFirstRow, null);
    }

    /**
     * 无 POJO，按指定标题读取指定 Sheet。
     */
    public static List<Map<String, Object>> readMap(String filePath, String[] titles, boolean ignoreFirstRow, StringInt sheet) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notEmpty(titles, "标题不能为空");
        int headRowNumber = ignoreFirstRow ? 1 : 0;
        List<Map<Integer, Object>> rawRows = readRawObjectRows(filePath, sheet, headRowNumber);
        return toObjectMaps(rawRows, titles);
    }

    /* ======================== 流式读取 ======================== */

    /**
     * 流式读取，每解析一行回调一次（同步阻塞，方法返回时已读完）。
     * <p>监听器不能被 Spring 管理，每次读取需新建。</p>
     */
    public static <T> void readEach(String filePath, Class<T> headClass, Consumer<T> rowConsumer) {
        readEach(filePath, headClass, null, rowConsumer);
    }

    /**
     * 流式读取指定 Sheet，每行回调一次。
     */
    public static <T> void readEach(String filePath, Class<T> headClass, StringInt sheet, Consumer<T> rowConsumer) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(rowConsumer, "行处理器不能为空");
        ReadListener<T> listener = new ReadListener<>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                rowConsumer.accept(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };
        sheet(FesodSheet.read(filePath, headClass, listener), sheet).doRead();
    }

    /**
     * 容错流式读取：某行解析失败时回调错误处理器并跳过该行，不中断整个读取。
     *
     * @param errorHandler 错误处理器，入参为行下标（0开始）与异常
     * @see TolerantReadListener
     */
    public static <T> void readEach(
            String filePath,
            Class<T> headClass,
            StringInt sheet,
            Consumer<T> rowConsumer,
            BiConsumer<Integer, Exception> errorHandler) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(rowConsumer, "行处理器不能为空");
        Assert.notNull(errorHandler, "错误处理器不能为空");
        TolerantReadListener<T> listener = new TolerantReadListener<>(rowConsumer, errorHandler);
        sheet(FesodSheet.read(filePath, headClass, listener), sheet).doRead();
    }

    /**
     * 分批流式读取，每批数据到达默认批次大小时回调一次（同步阻塞，方法返回时已读完）。
     */
    public static <T> void readBatch(String filePath, Class<T> headClass, Consumer<List<T>> batchConsumer) {
        readBatch(filePath, headClass, null, batchConsumer, DEFAULT_BATCH_SIZE);
    }

    /**
     * 分批流式读取，自定义批次大小。
     */
    public static <T> void readBatch(String filePath, Class<T> headClass, Consumer<List<T>> batchConsumer, int batchSize) {
        readBatch(filePath, headClass, null, batchConsumer, batchSize);
    }

    /**
     * 分批流式读取指定 Sheet。
     */
    public static <T> void readBatch(
            String filePath,
            Class<T> headClass,
            StringInt sheet,
            Consumer<List<T>> batchConsumer,
            int batchSize) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notNull(headClass, "数据类型不能为空");
        Assert.notNull(batchConsumer, "批次处理器不能为空");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批次大小必须大于0");
        }
        sheet(
                FesodSheet.read(filePath, headClass, new PageReadListener<>(batchConsumer, batchSize)),
                sheet
        ).doRead();
    }

    /* ======================== 内部辅助 ======================== */

    private static List<Map<Integer, String>> readRawStringRows(String filePath, StringInt sheet, int headRowNumber) {
        List<Map<Integer, String>> list = sheet(
                FesodSheet.read(filePath)
                        .readDefaultReturn(ReadDefaultReturnEnum.STRING)
                        .headRowNumber(headRowNumber),
                sheet
        ).doReadSync();
        return emptyIfNull(list);
    }

    private static List<Map<Integer, Object>> readRawObjectRows(String filePath, StringInt sheet, int headRowNumber) {
        List<Map<Integer, Object>> list = sheet(
                FesodSheet.read(filePath)
                        .readDefaultReturn(ReadDefaultReturnEnum.ACTUAL_DATA)
                        .headRowNumber(headRowNumber),
                sheet
        ).doReadSync();
        return emptyIfNull(list);
    }

    private static List<List<String>> toStringRows(List<Map<Integer, String>> rawRows) {
        if (CollectionAndMapUtils.isEmpty(rawRows)) {
            return Collections.emptyList();
        }
        List<List<String>> rows = new ArrayList<>(rawRows.size());
        for (Map<Integer, String> rawRow : rawRows) {
            rows.add(CollectionAndMapUtils.isEmpty(rawRow)
                    ? Collections.emptyList()
                    : toOrderedStringList(rawRow));
        }
        return rows;
    }

    private static List<Map<String, Object>> toObjectMaps(List<Map<Integer, Object>> rawRows, String[] titles) {
        if (CollectionAndMapUtils.isEmpty(rawRows)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>(rawRows.size());
        for (Map<Integer, Object> rawRow : rawRows) {
            Map<String, Object> row = new LinkedHashMap<>(titles.length);
            boolean hasValue = false;
            for (int i = 0; i < titles.length; i++) {
                Object value = rawRow == null ? null : rawRow.get(i);
                row.put(titles[i], value);
                if (value != null && !value.toString().trim().isEmpty()) {
                    hasValue = true;
                }
            }
            if (hasValue) {
                result.add(row);
            }
        }
        return result;
    }

    private static List<String> toOrderedStringList(Map<Integer, String> rawRow) {
        int maxIndex = rawRow.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        List<String> row = new ArrayList<>(maxIndex + 1);
        for (int i = 0; i <= maxIndex; i++) {
            row.add(rawRow.get(i));
        }
        return row;
    }

    /**
     * 按 {@link StringInt} 定位 Sheet，包级共享给同包的其他 Fesod 读取工具类。
     */
    static ExcelReaderSheetBuilder sheet(ExcelReaderBuilder readerBuilder, StringInt sheet) {
        if (sheet == null) {
            return readerBuilder.sheet();
        }
        if (sheet.is(Integer.class)) {
            return readerBuilder.sheet(sheet.as(Integer.class));
        }
        // StringInt 构造已保证非 Integer 即 String
        return readerBuilder.sheet(sheet.as(String.class));
    }

    private static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
