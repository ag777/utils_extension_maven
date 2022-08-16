package github.ag777.util.file.word;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description word操作工具类
 * @Date 2022/8/16 11:32
 */
public class WordUtils {
//    /**
//     * 根据模板和数据生成结果
//     * @param filePath 模板文件路径
//     * @param dataMap 疏忽
//     * @return 文档对象
//     * @throws Exception 异常
//     */
//    public static XWPFDocument parseDocx(String filePath, Map<String, Object> dataMap) throws Exception {
//        return WordExportUtil.exportWord07(filePath, dataMap);
//    }
//
//    /**
//     * 关闭文档
//     * @param doc 文档对象
//     */
//    public static void close(XWPFDocument doc) {
//        IOUtils.close(doc);
//    }
//
//    /**
//     * 将文档对象保存成文件,并关闭文件
//     * @param doc 文档对象
//     * @param outputFile
//     * @throws IOException 写出文件异常
//     */
//    public static void saveAndClose(XWPFDocument doc, File outputFile) throws IOException {
//        BufferedOutputStream out = new BufferedOutputStream(FileUtils.getOutputStream(outputFile));
//        try {
//            doc.write(out);
//        } finally {
//            IOUtils.close(doc, out);
//        }
//    }
//
//    /**
//     * @param filePath 图片路径
//     * @param width 宽度
//     * @param height 高度
//     * @return 模板替换用的图片对象
//     */
//    public static ImageEntity getImage(String filePath, int width, int height) {
//        ImageEntity image = new ImageEntity();
//        image.setWidth(width);
//        image.setHeight(height);
//        image.setUrl(filePath);
//        return image;
//    }
//
//    /**
//     * 设置单元格背景色
//     * @param cell 单元格
//     * @param color 颜色 比如想设置为#ff0000,则传入ff0000
//     */
//    public static void setBackgroundColor(XWPFTableCell cell, String color)  {
//        cell.getCTTc().addNewTcPr().addNewShd().setFill(color);
//    }
//
//    /**
//     * 获取单元格内容
//     * @param cell 单元格
//     * @return 内容
//     */
//    public static String getStr(XWPFTableCell cell) {
//        return cell.getText();
//    }
//
//    /**
//     * 获取单元格内容
//     * @param cell 单元格
//     * @param defaultValue 内容为空时返回的默认值
//     * @return 内容
//     */
//    public static String getStr(XWPFTableCell cell, String defaultValue) {
//        return StringUtils.defaultIfBlank(getStr(cell), defaultValue);
//    }
//
//    /**
//     * 获取单元格内容(Integer类型)
//     * @param cell 单元格
//     * @param defaultValue 内容为空或解析不出数字时返回的默认值
//     * @return 内容(Integer类型)
//     */
//    public static Integer getInt(XWPFTableCell cell, Integer defaultValue) {
//        return ObjectUtils.toInt(getStr(cell), defaultValue);
//    }
//
//    /**
//     * 获取单元格内容(Long类型)
//     * @param cell 单元格
//     * @param defaultValue 内容为空或解析不出数字时返回的默认值
//     * @return 内容(Long类型)
//     */
//    public static Long getLong(XWPFTableCell cell, Long defaultValue) {
//        return ObjectUtils.toLong(getStr(cell), defaultValue);
//    }
//
//    /**
//     * 获取单元格内容(Float类型)
//     * @param cell 单元格
//     * @param defaultValue 内容为空或解析不出数字时返回的默认值
//     * @return 内容(Float类型)
//     */
//    public static Float getFloat(XWPFTableCell cell, Float defaultValue) {
//        return ObjectUtils.toFloat(getStr(cell), defaultValue);
//    }
//
//    /**
//     * 获取单元格内容(Double类型)
//     * @param cell 单元格
//     * @param defaultValue 内容为空或解析不出数字时返回的默认值
//     * @return 内容(Double类型)
//     */
//    public static Double getFloat(XWPFTableCell cell, Double defaultValue) {
//        return ObjectUtils.toDouble(getStr(cell), defaultValue);
//    }

}
