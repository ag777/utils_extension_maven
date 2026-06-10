package github.ag777.util.file.excel.fesod.model;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;
import org.apache.fesod.sheet.annotation.format.NumberFormat;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fesod 读写的基础示例模型。
 * <p>
 * 演示最常见的注解组合，可直接配合 {@link github.ag777.util.file.excel.fesod.FesodReadUtils}
 * 与 {@link github.ag777.util.file.excel.fesod.FesodWriteUtils} 使用。
 * </p>
 *
 * <pre>{@code
 * // 写入
 * FesodWriteUtils.write("demo.xlsx", DemoExcelData.class, DemoExcelData.sampleList());
 *
 * // 同步读取
 * List<DemoExcelData> list = FesodReadUtils.read("demo.xlsx", DemoExcelData.class);
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
@Data
public class DemoExcelData {

    @ExcelProperty("字符串")
    private String text;

    @ExcelProperty("日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private Date date;

    @ExcelProperty("数字")
    @NumberFormat("#.##")
    private Double number;

    /** 不参与读写的字段 */
    @ExcelIgnore
    private String internalRemark;

    /**
     * 构造演示数据，供写入示例或单元测试使用。
     */
    public static List<DemoExcelData> sampleList() {
        List<DemoExcelData> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DemoExcelData row = new DemoExcelData();
            row.setText("文本" + i);
            row.setDate(new Date());
            row.setNumber(0.56 + i);
            row.setInternalRemark("仅内存字段-" + i);
            list.add(row);
        }
        return list;
    }
}
