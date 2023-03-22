package github.ag777.util.file.excel;

/**
 * 对easypoi的二次封装
 * 需要easypoi的依赖,由于和工程中的pdfbox依赖冲突，所以这里不引入
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/3/22 10:15
 */
public class ExcelUtils {

    private ExcelUtils() {}

//    /**
//     * 导出单个sheet的excel文件
//     * @param templatePath 模板文件路径，如果在resource下，不加第一个/
//     * @param outputFile 输出文件
//     * @param dataMap 数据
//     * @throws IOException IO异常
//     */
//    public static void export(String templatePath, File outputFile, Map<String, Object> dataMap) throws IOException {
//        TemplateExportParams params = new TemplateExportParams(templatePath);
//        //开启横向遍历 开启横向遍历 开启横向遍历
//        params.setColForEach(true);
//        Workbook workbook = ExcelExportUtil.exportExcel(params, dataMap);
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(outputFile);
//            workbook.write(out);
//        } finally {
//            IOUtils.close(out);
//        }
//    }
//
//    /**
//     * 导出含有多个sheet的excel文件
//     * @param templatePath 模板文件路径
//     * @param outputFile 输出文件路径
//     * @param dataList 数据，每项对应sheet中的值
//     * @throws IOException io异常
//     */
//    public static File exportWithMultiSheet(String templatePath, File outputFile, List<Map<String, Object>> dataList) throws IOException {
//        TemplateExportParams params = new TemplateExportParams(templatePath);
//        // 开启横向遍历 开启横向遍历 开启横向遍历
//        params.setColForEach(true);
//        // 设置sheet页对应角标
//        params.setSheetNum(IntStream.range(0, dataList.size()).boxed().toArray(Integer[]::new));
//        // 构造数据
//        Map<Integer, Map<String, Object>> realMap = new HashMap<>();
//        for (int i = 0; i < dataList.size(); i++) {
//            Map<String, Object> map = dataList.get(i);
//            if (!MapUtils.isEmpty(map)) {
//                realMap.put(i, map);
//            }
//        }
//        Workbook workbook = ExcelExportUtil.exportExcel(realMap, params);
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(outputFile);
//            workbook.write(out);
//            return outputFile;
//        } finally {
//            IOUtils.close(workbook, out);
//        }
//    }
}
