package github.ag777.util.file.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/24 16:47
 */
public class ExcelUtils {

    private static final float MAX_ROW_HEIGHT_POINTS = 409f;
    private static final float ROW_HEIGHT_PADDING = 1.15f;

    private ExcelUtils() {}

    public static void adjustAllRowHeightsBasedOnContent(File file) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        byte[] sourceBytes = Files.readAllBytes(file.toPath());
        byte[] targetBytes;
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(sourceBytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            adjustAllRowHeightsBasedOnContent(workbook);
            workbook.write(outputStream);
            targetBytes = outputStream.toByteArray();
        }
        Files.write(file.toPath(), targetBytes);
    }

    /**
     * 遍历工作簿的所有行，根据每行内容和列宽动态调整行高。
     *
     * @param workbook 工作簿
     */
    public static void adjustAllRowHeightsBasedOnContent(Workbook workbook) {
        if (workbook == null) {
            return;
        }
        DataFormatter formatter = new DataFormatter();
        Map<CellStyle, CellStyle> wrapStyleCache = new IdentityHashMap<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            adjustAllRowHeightsBasedOnContent(workbook.getSheetAt(i), formatter, wrapStyleCache);
        }
    }

    /**
     * 遍历工作表的所有行，根据每行内容和列宽动态调整行高。
     *
     * @param sheet 需要调整行高的工作表
     */
    public static void adjustAllRowHeightsBasedOnContent(Sheet sheet) {
        if (sheet == null) {
            return;
        }
        adjustAllRowHeightsBasedOnContent(sheet, new DataFormatter(), new IdentityHashMap<>());
    }

    private static void adjustAllRowHeightsBasedOnContent(Sheet sheet, DataFormatter formatter, Map<CellStyle, CellStyle> wrapStyleCache) {
        if (sheet == null) {
            return;
        }
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            adjustRowHeightBasedOnContent(sheet.getRow(i), formatter, wrapStyleCache);
        }
    }

    /**
     * 根据行内容、列宽和合并单元格宽度动态调整行高。
     *
     * @param row 需要调整行高的行
     */
    public static void adjustRowHeightBasedOnContent(Row row) {
        if (row == null) {
            return;
        }
        adjustRowHeightBasedOnContent(row, new DataFormatter(), new IdentityHashMap<>());
    }

    private static void adjustRowHeightBasedOnContent(Row row, DataFormatter formatter, Map<CellStyle, CellStyle> wrapStyleCache) {
        if (row == null) {
            return;
        }
        Sheet sheet = row.getSheet();
        int maxLines = 1;
        for (Cell cell : row) {
            String text = formatter.formatCellValue(cell);
            if (text == null || text.isBlank()) {
                continue;
            }
            cell.setCellStyle(getWrapCellStyle(cell, wrapStyleCache));
            int columnWidthChars = getMergedColumnWidthChars(sheet, cell);
            maxLines = Math.max(maxLines, estimateLineCount(text, columnWidthChars));
        }
        float baseHeight = row.getHeightInPoints() > 0 ? row.getHeightInPoints() : sheet.getDefaultRowHeightInPoints();
        float targetHeight = Math.max(baseHeight, sheet.getDefaultRowHeightInPoints() * maxLines * ROW_HEIGHT_PADDING);
        row.setHeightInPoints(Math.min(targetHeight, MAX_ROW_HEIGHT_POINTS));
    }

    private static CellStyle getWrapCellStyle(Cell cell, Map<CellStyle, CellStyle> cache) {
        CellStyle source = cell.getCellStyle();
        if (source.getWrapText()) {
            return source;
        }
        CellStyle wrapped = cache.get(source);
        if (wrapped == null) {
            wrapped = cell.getSheet().getWorkbook().createCellStyle();
            wrapped.cloneStyleFrom(source);
            wrapped.setWrapText(true);
            cache.put(source, wrapped);
        }
        return wrapped;
    }

    private static int getMergedColumnWidthChars(Sheet sheet, Cell cell) {
        int firstCol = cell.getColumnIndex();
        int lastCol = cell.getColumnIndex();
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                firstCol = range.getFirstColumn();
                lastCol = range.getLastColumn();
                break;
            }
        }
        int widthUnits = 0;
        for (int col = firstCol; col <= lastCol; col++) {
            widthUnits += sheet.getColumnWidth(col);
        }
        return Math.max(1, widthUnits / 256);
    }

    private static int estimateLineCount(String text, int columnWidthChars) {
        int lineCount = 0;
        String[] lines = text.split("\\R", -1);
        for (String line : lines) {
            int weightedLength = line.codePoints()
                    .map(ExcelUtils::getDisplayWidth)
                    .sum();
            lineCount += Math.max(1, (int) Math.ceil(weightedLength / (double) columnWidthChars));
        }
        return Math.max(1, lineCount);
    }

    private static int getDisplayWidth(int codePoint) {
        Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
        return switch (script) {
            case HAN, HIRAGANA, KATAKANA, HANGUL -> 2;
            default -> 1;
        };
    }

    /**
     * 调整指定工作表中所有列的宽度以适应其内容。
     *
     * @param sheet 需要调整列宽的工作表
     */
    public static void autoAdjustColumnsWidth(Sheet sheet) {
        if (sheet == null) {
            return;
        }
        int maxColumnCount = 0;
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && row.getLastCellNum() > maxColumnCount) {
                maxColumnCount = row.getLastCellNum();
            }
        }
        for (int columnIndex = 0; columnIndex < maxColumnCount; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
            int currentWidth = sheet.getColumnWidth(columnIndex);
            sheet.setColumnWidth(columnIndex, currentWidth + 512);
        }
    }
}
