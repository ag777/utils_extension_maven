package github.ag777.util.file.excel.fesod.converter;

import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.converters.ReadConverterContext;
import org.apache.fesod.sheet.converters.WriteConverterContext;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.data.WriteCellData;

/**
 * 自定义转换器示例：Java 中的 {@code Integer}（1/0）与 Excel 中的 "男"/"女" 互转。
 * <p>
 * 通过 {@code @ExcelProperty(converter = GenderConverter.class)} 标注在字段上生效，
 * 也可经 builder 的 {@code registerConverter} 全局注册。
 * </p>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public class GenderConverter implements Converter<Integer> {

    public static final int MALE = 1;
    public static final int FEMALE = 0;
    private static final String MALE_TEXT = "男";
    private static final String FEMALE_TEXT = "女";

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadConverterContext<?> context) {
        String text = context.getReadCellData().getStringValue();
        if (text == null) {
            return null;
        }
        return MALE_TEXT.equals(text.trim()) ? MALE : FEMALE;
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) {
        Integer value = context.getValue();
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value == MALE ? MALE_TEXT : FEMALE_TEXT);
    }
}
