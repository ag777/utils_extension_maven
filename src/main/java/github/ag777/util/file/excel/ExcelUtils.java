package github.ag777.util.file.excel;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.collection.MapUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * 对easypoi的二次封装
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/3/22 10:15
 */
public class ExcelUtils {

    private ExcelUtils() {}

    public static void main(String[] args) throws IOException {
        export("D:\\b.xlsx", new File("d:/c.xlsx"), MapUtils.of("a", 1));
    }

    /**
     * 导出单个sheet的excel文件
     * @param templatePath 模板文件路径，如果在resource下，不加第一个/
     * @param outputFile 输出文件
     * @param dataMap 数据
     * @throws IOException IO异常
     */
    public static void export(String templatePath, File outputFile, Map<String, Object> dataMap) throws IOException {
        TemplateExportParams params = new TemplateExportParams(templatePath);
        //开启横向遍历 开启横向遍历 开启横向遍历
        params.setColForEach(true);
        Workbook workbook = ExcelExportUtil.exportExcel(params, dataMap);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            workbook.write(out);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
     * 导出含有多个sheet的excel文件
     * @param templatePath 模板文件路径
     * @param outputFile 输出文件路径
     * @param dataList 数据，每项对应sheet中的值
     * @return 根据模板生成的文件
     * @throws IOException io异常
     */
    public static File exportWithMultiSheet(String templatePath, File outputFile, List<Map<String, Object>> dataList) throws IOException {
        TemplateExportParams params = new TemplateExportParams(templatePath);
        // 开启横向遍历 开启横向遍历 开启横向遍历
        params.setColForEach(true);
        // 设置sheet页对应角标
        params.setSheetNum(IntStream.range(0, dataList.size()).boxed().toArray(Integer[]::new));
        // 构造数据
        Map<Integer, Map<String, Object>> realMap = new HashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> map = dataList.get(i);
            if (!MapUtils.isEmpty(map)) {
                realMap.put(i, map);
            }
        }
        return exportExcel(realMap, params, outputFile);
    }

    /**
     * 导出含有多个sheet的excel文件(所有页共用一个数据,自动读取模板获取sheet数量)
     * @param templatePath 模板文件路径
     * @param outputFile 输出文件路径
     * @param dataMap 数据map
     * @return 根据模板生成的文件
     * @throws IOException io异常
     */
    public static File exportWithMultiSheet(String templatePath, File outputFile, Map<String, Object> dataMap) throws IOException {
        int sheetCount = getSheetCount(new File(templatePath));
        return exportWithMultiSheet(templatePath, outputFile, dataMap, sheetCount);
    }

    /**
     * 导出含有多个sheet的excel文件(所有页共用一个数据)
     * @param templatePath 模板文件路径
     * @param outputFile 输出文件路径
     * @param dataMap 数据map
     * @param sheetCount 模板sheet数量
     * @return 根据模板生成的文件
     * @throws IOException io异常
     */
    public static File exportWithMultiSheet(String templatePath, File outputFile, Map<String, Object> dataMap, int sheetCount) throws IOException {
        TemplateExportParams params = new TemplateExportParams(templatePath);

        // 开启横向遍历 开启横向遍历 开启横向遍历
        params.setColForEach(true);
        // 设置sheet页对应角标
        params.setSheetNum(IntStream.range(0, sheetCount).boxed().toArray(Integer[]::new));
        // 构造数据
        Map<Integer, Map<String, Object>> realMap = new HashMap<>();
        for (int i = 0; i < sheetCount; i++) {
            realMap.put(i, dataMap);
        }
        return exportExcel(realMap, params, outputFile);
    }

    public static int getSheetCount(File excelFile) throws IOException {
        Workbook wb = WorkbookFactory.create(excelFile);
        try {
            return wb.getNumberOfSheets();
        } finally {
            IOUtils.close(wb);
        }
    }

    public static File exportExcel(Map<Integer, Map<String, Object>> sheetDataMap, TemplateExportParams params, File outputFile) throws IOException {
        Workbook workbook = ExcelExportUtil.exportExcel(sheetDataMap, params);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            workbook.write(out);
            return outputFile;
        } finally {
            IOUtils.close(workbook, out);
        }
    }


}
