package github.ag777.util.file.word;


import github.ag777.util.file.word.model.PicInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.function.Function;

/**
 * Word文档工具类统一入口
 * 自动识别文档格式并调用相应的工具类
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2025年09月11日,last modify at 2025年09月12日
 */
public class WordUtils {
    
    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        DOC,    // 旧版Word文档(.doc)
        DOCX,   // 新版Word文档(.docx)  
        UNKNOWN // 未知格式
    }
    
    /**
     * 检测文档类型
     * @param inputStream 文档输入流
     * @return 文档类型
     * @throws IOException 读取文件异常
     */
    public static DocumentType detectDocumentType(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            throw new IllegalArgumentException("InputStream must support mark/reset");
        }
        
        inputStream.mark(8); // 标记当前位置，准备读取文件头
        
        try {
            byte[] header = new byte[8];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead >= 4) {
                // 检查DOCX格式 (ZIP文件头: PK)
                if (header[0] == 0x50 && header[1] == 0x4B) {
                    return DocumentType.DOCX;
                }
                
                // 检查DOC格式 (OLE2文件头)
                if (header[0] == (byte)0xD0 && header[1] == (byte)0xCF && 
                    header[2] == 0x11 && header[3] == (byte)0xE0) {
                    return DocumentType.DOC;
                }
            }
            
            return DocumentType.UNKNOWN;
        } finally {
            inputStream.reset(); // 重置流位置
        }
    }
    
    /**
     * 检测文档类型（通过文件名）
     * @param fileName 文件名
     * @return 文档类型
     */
    public static DocumentType detectDocumentType(String fileName) {
        if (fileName == null) {
            return DocumentType.UNKNOWN;
        }
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".docx")) {
            return DocumentType.DOCX;
        } else if (lowerName.endsWith(".doc")) {
            return DocumentType.DOC;
        }
        
        return DocumentType.UNKNOWN;
    }
    
    
    /**
     * 判断输入流是否为Word文档
     * @param inputStream 文件输入流（必须支持mark/reset）
     * @return true如果是Word文档
     * @throws IOException 读取文件异常
     */
    public static boolean isWordDocument(InputStream inputStream) throws IOException {
        DocumentType type = detectDocumentType(inputStream);
        return type == DocumentType.DOC || type == DocumentType.DOCX;
    }
    
    /**
     * 判断文件名是否为Word文档
     * @param fileName 文件名
     * @return true如果是Word文档
     */
    public static boolean isWordDocument(String fileName) {
        DocumentType type = detectDocumentType(fileName);
        return type == DocumentType.DOC || type == DocumentType.DOCX;
    }
    
    // ================= HTML转换相关方法 =================
    
    /**
     * 将Word文档转换为HTML字符串
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 文件名
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName) throws Exception {
        return convertToHtml(inputStream, fileName, true, null);
    }
    
    /**
     * 将Word文档转换为HTML字符串（可控制图片处理）
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 文件名
     * @param processImages 是否处理图片
     * @param imageHandler 图片处理器，为null时使用默认处理器
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName, 
                                     boolean processImages, Function<PicInfo, String> imageHandler) throws Exception {
        DocumentType type = detectDocumentType(inputStream);
        
        switch (type) {
            case DOCX:
                return DocxUtils.convertToHtml(inputStream, fileName, processImages, imageHandler);
            case DOC:
                return DocUtils.convertToHtml(inputStream, fileName, processImages, imageHandler);
            default:
                throw new UnsupportedOperationException("Unsupported document format");
        }
    }
    
    /**
     * 将Word文档转换为HTML并保存到文件
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 原文件名
     * @param htmlFilePath 输出HTML文件路径
     * @throws Exception 转换或保存异常
     */
    public static void convertToHtmlFile(InputStream inputStream, String fileName, 
                                        String htmlFilePath) throws Exception {
        String html = convertToHtml(inputStream, fileName);
            
        // 保存到文件
        java.nio.file.Files.write(
            java.nio.file.Paths.get(htmlFilePath), 
            html.getBytes("UTF-8")
        );
    }
    
    // ================= 图片处理工具方法 =================
    
    /**
     * 获取默认的图片处理器（转换为base64内嵌图片）
     * @return 默认图片处理器
     */
    public static Function<PicInfo, String> getDefaultImageHandler() {
        return picInfo -> {
            if (!picInfo.isValid()) {
                return "<p><em>[图片无法显示]</em></p>";
            }
            
            // 转换为base64
            String base64Image = Base64.getEncoder().encodeToString(picInfo.getData());
            
            // 生成HTML img标签
            StringBuilder imgHtml = new StringBuilder();
            imgHtml.append("<img src=\"data:").append(picInfo.getMimeType()).append(";base64,")
                   .append(base64Image).append("\"");
            
            if (picInfo.getFileName() != null && !picInfo.getFileName().isEmpty()) {
                String escapedName = escapeHtml(picInfo.getFileName());
                imgHtml.append(" alt=\"").append(escapedName).append("\"");
                imgHtml.append(" title=\"").append(escapedName).append("\"");
            } else {
                imgHtml.append(" alt=\"Word图片\"");
            }
            
            imgHtml.append(" />");
            
            // 如果有文件名，添加图片说明
            if (picInfo.getFileName() != null && !picInfo.getFileName().isEmpty()) {
                imgHtml.append("<div class=\"image-caption\">")
                       .append(escapeHtml(picInfo.getFileName()))
                       .append("</div>");
            }
            
            return imgHtml.toString();
        };
    }
    
    /**
     * 获取简单的图片处理器（只显示图片，不显示说明）
     * @return 简单图片处理器
     */
    public static Function<PicInfo, String> getSimpleImageHandler() {
        return picInfo -> {
            if (!picInfo.isValid()) {
                return "<p><em>[图片无法显示]</em></p>";
            }
            
            String base64Image = Base64.getEncoder().encodeToString(picInfo.getData());
            return "<img src=\"data:" + picInfo.getMimeType() + ";base64," + base64Image + "\" alt=\"图片\" />";
        };
    }
    
    /**
     * 获取忽略图片的处理器（用于不显示图片的场景）
     * @return 忽略图片的处理器
     */
    public static Function<PicInfo, String> getIgnoreImageHandler() {
        return picInfo -> "<p><em>[此处有图片]</em></p>";
    }
    
    /**
     * HTML转义
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
}
