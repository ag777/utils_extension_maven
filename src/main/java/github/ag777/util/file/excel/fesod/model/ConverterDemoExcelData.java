package github.ag777.util.file.excel.fesod.model;

import github.ag777.util.file.excel.fesod.converter.GenderConverter;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义转换器示例模型：性别字段在 Java 中为 1/0，写入 Excel 时显示 "男"/"女"，读取时自动转回。
 *
 * <pre>{@code
 * FesodWriteUtils.write("converter.xlsx", ConverterDemoExcelData.class, ConverterDemoExcelData.sampleList());
 * List<ConverterDemoExcelData> list = FesodReadUtils.read("converter.xlsx", ConverterDemoExcelData.class);
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see GenderConverter
 */
@Data
public class ConverterDemoExcelData {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty(value = "性别", converter = GenderConverter.class)
    private Integer gender;

    public static List<ConverterDemoExcelData> sampleList() {
        List<ConverterDemoExcelData> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ConverterDemoExcelData row = new ConverterDemoExcelData();
            row.setName("员工" + i);
            row.setGender(i % 2 == 0 ? GenderConverter.MALE : GenderConverter.FEMALE);
            list.add(row);
        }
        return list;
    }
}
