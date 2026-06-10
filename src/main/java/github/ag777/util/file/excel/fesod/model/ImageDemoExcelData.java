package github.ag777.util.file.excel.fesod.model;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import org.apache.fesod.sheet.annotation.write.style.ContentRowHeight;
import org.apache.fesod.sheet.converters.string.StringImageConverter;

import java.io.File;
import java.net.URL;

/**
 * 图片导出示例模型：字段类型为 {@link File}、{@code byte[]}、{@link URL} 时自动以图片写入单元格；
 * {@code String} 类型路径需显式指定 {@link StringImageConverter}。
 *
 * <pre>{@code
 * ImageDemoExcelData row = new ImageDemoExcelData();
 * row.setFile(new File("path/to/image.jpg"));
 * row.setUrl(new URL("https://example.com/image.jpg"));
 * FesodWriteUtils.write("image.xlsx", ImageDemoExcelData.class, List.of(row));
 * }</pre>
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 * @see <a href="https://fesod.apache.org/zh-cn/docs/sheet/write/image/">Fesod 图片导出</a>
 */
@Data
@ContentRowHeight(100)
@ColumnWidth(25)
public class ImageDemoExcelData {

    @ExcelProperty("本地文件")
    private File file;

    @ExcelProperty("字节数组")
    private byte[] byteArray;

    @ExcelProperty("网络图片")
    private URL url;

    /** String 路径默认按文本写入，需指定转换器才会作为图片处理 */
    @ExcelProperty(value = "路径图片", converter = StringImageConverter.class)
    private String path;
}
