package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;
import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.merge.LoopMergeStrategy;
import org.apache.fesod.sheet.write.merge.OnceAbsoluteMergeStrategy;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteWorkbookHolder;
import org.apache.fesod.sheet.write.metadata.style.WriteCellStyle;
import org.apache.fesod.sheet.write.metadata.style.WriteFont;
import org.apache.fesod.sheet.write.style.HorizontalCellStyleStrategy;
import org.apache.fesod.sheet.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * 常用 {@link org.apache.fesod.sheet.write.handler.WriteHandler} 工厂。
 * <p>
 * 配合 {@link FesodWriteAdvanceUtils#write} 或 builder 的 {@code registerWriteHandler} 使用，
 * 覆盖样式策略、自适应列宽、冻结窗格、下拉框、合并单元格等高频写入需求。
 * </p>
 *
 * <pre>{@code
 * FesodWriteAdvanceUtils.write("out.xlsx", DemoExcelData.class, null, data,
 *         FesodWriteHandlerUtils.simpleStyle(IndexedColors.LIGHT_BLUE, (short) 12),
 *         FesodWriteHandlerUtils.autoColumnWidth(),
 *         FesodWriteHandlerUtils.freezePane(0, 1),
 *         FesodWriteHandlerUtils.dropdown(2, 1, 100, "在职", "离职"));
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class FesodWriteHandlerUtils {

    private FesodWriteHandlerUtils() {}

    /**
     * 简单样式策略：表头加背景色、加粗，内容居中。
     *
     * @param headBackground         表头背景色
     * @param headFontHeightInPoints 表头字号
     */
    public static HorizontalCellStyleStrategy simpleStyle(IndexedColors headBackground, short headFontHeightInPoints) {
        Assert.notNull(headBackground, "表头背景色不能为空");
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(headBackground.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints(headFontHeightInPoints);
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);

        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 自适应列宽策略（按已写入内容的最长长度匹配）。
     */
    public static LongestMatchColumnWidthStyleStrategy autoColumnWidth() {
        return new LongestMatchColumnWidthStyleStrategy();
    }

    /**
     * 冻结窗格。
     *
     * @param colSplit 冻结的列数（0 表示不冻结列）
     * @param rowSplit 冻结的行数（如 1 表示冻结表头行）
     */
    public static SheetWriteHandler freezePane(int colSplit, int rowSplit) {
        return new SheetWriteHandler() {
            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                writeSheetHolder.getSheet().createFreezePane(colSplit, rowSplit);
            }
        };
    }

    /**
     * 给指定列加下拉框（数据校验）。
     *
     * @param columnIndex 列下标（0开始）
     * @param firstRow    生效起始行下标（0开始，通常为 1 以跳过表头）
     * @param lastRow     生效结束行下标
     * @param options     下拉选项
     */
    public static SheetWriteHandler dropdown(int columnIndex, int firstRow, int lastRow, String... options) {
        Assert.notEmpty(options, "下拉选项不能为空");
        return new SheetWriteHandler() {
            @Override
            public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
                Sheet sheet = writeSheetHolder.getSheet();
                DataValidationHelper helper = sheet.getDataValidationHelper();
                CellRangeAddressList range = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
                DataValidation validation = helper.createValidation(
                        helper.createExplicitListConstraint(options), range);
                sheet.addValidationData(validation);
            }
        };
    }

    /**
     * 合并指定区域（一次性绝对合并）。
     */
    public static OnceAbsoluteMergeStrategy mergeOnce(int firstRow, int lastRow, int firstColumn, int lastColumn) {
        return new OnceAbsoluteMergeStrategy(firstRow, lastRow, firstColumn, lastColumn);
    }

    /**
     * 指定列每 eachRow 行循环合并（与模型注解 {@code @ContentLoopMerge} 等效的编程式写法）。
     */
    public static LoopMergeStrategy loopMerge(int eachRow, int columnIndex) {
        return new LoopMergeStrategy(eachRow, columnIndex);
    }
}
