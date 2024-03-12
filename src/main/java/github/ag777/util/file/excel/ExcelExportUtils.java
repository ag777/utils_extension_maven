package github.ag777.util.file.excel;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.collection.MapUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * 对easypoi的二次封装
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/3/12 11:34
 */
public class ExcelExportUtils {

    private ExcelExportUtils() {}

    public static void main(String[] args) throws IOException {
        export("D:\\b.xlsx", new File("d:/c.xlsx"), MapUtils.of("a", 1));
    }

    /**
     * 导出单个sheet的excel文件
     *
     * @param templatePath 模板文件路径，如果在resource下，不加第一个/
     * @param outputFile   输出文件
     * @param dataMap      数据
     * @return excel文件
     * @throws IOException IO异常
     */
    public static File export(String templatePath, File outputFile, Map<String, Object> dataMap) throws ExcelExportException, IOException {
        return export(templatePath, outputFile, dataMap, null);
    }

    /**
     * 导出单个sheet的excel文件
     *
     * @param templatePath    模板文件路径，如果在resource下，不加第一个/
     * @param outputFile      输出文件
     * @param dataMap         数据
     * @param workbookHandler 处理workbook的回调
     * @return excel文件
     * @throws IOException IO异常
     */
    public static File export(String templatePath, File outputFile, Map<String, Object> dataMap, Consumer<Workbook> workbookHandler) throws ExcelExportException, IOException {
        Workbook workbook = export2WorkBook(templatePath, dataMap);
        try {
            if (workbookHandler != null) {
                workbookHandler.accept(workbook);
            }
            return write2File(workbook, outputFile);
        } finally {
            IOUtils.close(workbook);
        }
    }

    public static Workbook export2WorkBook(String templatePath, Map<String, Object> dataMap) throws ExcelExportException {
        TemplateExportParams params = new TemplateExportParams(templatePath);
        // 开启横向遍历 开启横向遍历 开启横向遍历
        params.setColForEach(true);
        Workbook wb = ExcelExportUtil.exportExcel(params, dataMap);
        if (wb == null) {
            throw new ExcelExportException("导出excel异常，详情请看日志");
        }
        return wb;
    }

    /**
     * 导出含有多个sheet的excel文件
     * @param templatePath 模板文件路径
     * @param outputFile 输出文件路径
     * @param dataList 数据，每项对应sheet中的值
     * @return 根据模板生成的文件
     * @throws IOException io异常
     */
    public static File exportWithMultiSheet(String templatePath, File outputFile, List<Map<String, Object>> dataList) throws ExcelExportException, IOException {
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
    public static File exportWithMultiSheet(String templatePath, File outputFile, Map<String, Object> dataMap) throws ExcelExportException, IOException {
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
    public static File exportWithMultiSheet(String templatePath, File outputFile, Map<String, Object> dataMap, int sheetCount) throws ExcelExportException, IOException {
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

    public static File exportExcel(Map<Integer, Map<String, Object>> sheetDataMap, TemplateExportParams params, File outputFile) throws ExcelExportException, IOException {
        Workbook workbook = ExcelExportUtil.exportExcel(sheetDataMap, params);
        return write2File(workbook, outputFile);
    }

    public static File write2File(Workbook wb, File outputFile) throws IOException {
        FileOutputStream out = null;
        try {
            // 创建父文件夹
            outputFile.getParentFile().mkdirs();
            out = new FileOutputStream(outputFile);
            // 写出文件
            wb.write(out);
            return outputFile;
        } finally {
            IOUtils.close(wb, out);
        }
    }


}
