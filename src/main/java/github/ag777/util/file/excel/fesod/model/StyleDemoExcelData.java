package github.ag777.util.file.excel.fesod.model;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.*;
import org.apache.fesod.sheet.enums.poi.FillPatternTypeEnum;
import org.apache.fesod.sheet.enums.poi.HorizontalAlignmentEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解式样式示例模型：表头背景色/字号、行高、内容对齐、循环合并。
 * <p>
 * 与编程式的 {@link github.ag777.util.file.excel.fesod.FesodWriteHandlerUtils#simpleStyle} 等效，
 * 注解方式适合样式固定的导出模板，编程式适合运行时动态决定样式。
 * </p>
 *
 * <pre>{@code
 * FesodWriteUtils.write("style.xlsx", StyleDemoExcelData.class, StyleDemoExcelData.sampleList());
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
@Data
@HeadRowHeight(25)
@ContentRowHeight(20)
// 表头浅蓝底（颜色下标见 org.apache.poi.ss.usermodel.IndexedColors）
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 44)
@HeadFontStyle(fontHeightInPoints = 13)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class StyleDemoExcelData {

    /** 同部门每 2 行合并为一格 */
    @ExcelProperty("部门")
    @ContentLoopMerge(eachRow = 2)
    private String department;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("工号")
    private String workNo;

    public static List<StyleDemoExcelData> sampleList() {
        List<StyleDemoExcelData> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            StyleDemoExcelData row = new StyleDemoExcelData();
            row.setDepartment("部门" + i / 2);
            row.setName("员工" + i);
            row.setWorkNo("NO" + (1000 + i));
            list.add(row);
        }
        return list;
    }
}
