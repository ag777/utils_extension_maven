package github.ag777.util.file.excel;

/**
 * 需要easypoi的依赖,由于和工程中的pdfbox依赖冲突，所以这里不引入
 * @author ag777 <837915770@vip.qq.com>
 * @Description 对easypoi的二次封装
 * @Date 2022/3/11 16:14
 */
public class EasyPoiUtils {

    private EasyPoiUtils() {}

//    /**
//     *
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
}
