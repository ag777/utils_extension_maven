package github.ag777.util.file.excel.fesod;

import github.ag777.util.lang.exception.Assert;

import java.util.List;

/**
 * 多 Sheet 写入时的单个 Sheet 参数，Sheet 位置由其在列表中的顺序决定。
 *
 * <pre>{@code
 * List<FesodSheetWriteParam<DemoExcelData>> sheets = List.of(
 *     FesodSheetWriteParam.of("汇总", DemoExcelData.sampleList()),
 *     FesodSheetWriteParam.of("明细", DemoExcelData.sampleList())
 * );
 * FesodWriteUtils.writeMultiSheet("out.xlsx", DemoExcelData.class, sheets);
 * }</pre>
 *
 * @param sheetName Sheet 名称
 * @param data      行数据
 * @param <T>       行数据类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2026/6/10
 */
public record FesodSheetWriteParam<T>(String sheetName, List<T> data) {

    public static <T> FesodSheetWriteParam<T> of(String sheetName, List<T> data) {
        Assert.notBlank(sheetName, "sheet名称不能为空");
        return new FesodSheetWriteParam<>(sheetName, data);
    }
}
