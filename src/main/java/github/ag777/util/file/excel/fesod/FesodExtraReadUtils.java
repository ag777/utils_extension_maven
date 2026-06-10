package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import github.ag777.util.lang.type.impl.StringInt;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.enums.CellExtraTypeEnum;
import org.apache.fesod.sheet.metadata.CellExtra;
import org.apache.fesod.sheet.read.builder.ExcelReaderBuilder;
import org.apache.fesod.sheet.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Apache Fesod 的额外信息读取工具类：批注、超链接、合并单元格范围。
 * <p>
 * 额外信息默认不解析，需通过 {@code extraRead(CellExtraTypeEnum)} 显式开启，
 * 解析结果经 {@link ReadListener#extra} 回调返回。
 * </p>
 *
 * <pre>{@code
 * // 读取合并单元格范围
 * List<CellExtra> merges = FesodExtraReadUtils.readMergeRanges("demo.xlsx", null);
 * merges.forEach(m -> System.out.printf("行%d-%d 列%d-%d%n",
 *         m.getFirstRowIndex(), m.getLastRowIndex(),
 *         m.getFirstColumnIndex(), m.getLastColumnIndex()));
 *
 * // 同时读取批注与超链接
 * List<CellExtra> extras = FesodExtraReadUtils.readExtras("demo.xlsx", null,
 *         CellExtraTypeEnum.COMMENT, CellExtraTypeEnum.HYPERLINK);
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/read/extra/">Fesod 额外信息读取</a>
 */
public class FesodExtraReadUtils {

    private FesodExtraReadUtils() {}

    /**
     * 读取合并单元格范围信息。
     *
     * @param sheet {@link StringInt#of(Integer)} 为下标，{@link StringInt#of(String)} 为名称；null 表示第一个 Sheet
     */
    public static List<CellExtra> readMergeRanges(String filePath, StringInt sheet) {
        return readExtras(filePath, sheet, CellExtraTypeEnum.MERGE);
    }

    /**
     * 读取批注信息。
     */
    public static List<CellExtra> readComments(String filePath, StringInt sheet) {
        return readExtras(filePath, sheet, CellExtraTypeEnum.COMMENT);
    }

    /**
     * 读取超链接信息。
     */
    public static List<CellExtra> readHyperlinks(String filePath, StringInt sheet) {
        return readExtras(filePath, sheet, CellExtraTypeEnum.HYPERLINK);
    }

    /**
     * 读取指定类型的额外信息（可同时开启多种类型）。
     *
     * @param types 额外信息类型：COMMENT 批注 / HYPERLINK 超链接 / MERGE 合并单元格
     */
    public static List<CellExtra> readExtras(String filePath, StringInt sheet, CellExtraTypeEnum... types) {
        Assert.notBlank(filePath, "文件路径不能为空");
        Assert.notEmpty(types, "额外信息类型不能为空");
        List<CellExtra> extras = new ArrayList<>();
        ReadListener<Object> listener = new ReadListener<>() {
            @Override
            public void invoke(Object data, AnalysisContext context) {
            }

            @Override
            public void extra(CellExtra extra, AnalysisContext context) {
                extras.add(extra);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        };
        ExcelReaderBuilder readerBuilder = FesodSheet.read(filePath).registerReadListener(listener);
        for (CellExtraTypeEnum type : types) {
            readerBuilder.extraRead(type);
        }
        FesodReadUtils.sheet(readerBuilder, sheet).doRead();
        return extras;
    }
}
