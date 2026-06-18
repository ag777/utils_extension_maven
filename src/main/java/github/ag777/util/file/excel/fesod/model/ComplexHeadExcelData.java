package github.ag777.util.file.excel.fesod.model;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 复杂（多级）表头写入示例模型。
 * <p>
 * {@code @ExcelProperty} 传入字符串数组即可生成多级表头，效果等价于官方文档中的 ComplexHeadData。
 * </p>
 *
 * <pre>{@code
 * FesodWriteUtils.write("complex.xlsx", ComplexHeadExcelData.class, ComplexHeadExcelData.sampleList());
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
@Data
public class ComplexHeadExcelData {

    @ExcelProperty({"基本信息", "姓名"})
    private String name;

    @ExcelProperty({"基本信息", "年龄"})
    private Integer age;

    @ExcelProperty({"薪资信息", "月薪"})
    private Double salary;

    @ExcelProperty({"薪资信息", "入职日期"})
    private Date hireDate;

    public static List<ComplexHeadExcelData> sampleList() {
        List<ComplexHeadExcelData> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ComplexHeadExcelData row = new ComplexHeadExcelData();
            row.setName("员工" + i);
            row.setAge(20 + i);
            row.setSalary(8000D + i * 500);
            row.setHireDate(new Date());
            list.add(row);
        }
        return list;
    }
}
