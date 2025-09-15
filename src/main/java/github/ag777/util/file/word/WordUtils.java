package github.ag777.util.file.word;


import github.ag777.util.file.word.model.PicInfo;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.util.Base64;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Word文档工具类统一入口
 * 自动识别文档格式并调用相应的工具类
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2025年09月11日,last modify at 2025年09月15日
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
     * 检测文档类型（基于文件）
     * 自动管理文件流的生命周期
     * @param file 文件
     * @return 文档类型
     * @throws IOException 读取文件异常
     */
    public static DocumentType detectDocumentType(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return detectDocumentType(fis);
        }
    }

    
    /**
     * 检测文档类型
     * 使用Apache POI的FileMagic进行精确的Word文档检测，不依赖文件扩展名
     * 
     * <p><strong>注意：</strong>此方法不会关闭传入的InputStream，调用者负责资源管理</p>
     * 
     * @param inputStream 文档输入流（调用者负责关闭）
     * @return 文档类型
     * @throws IOException 读取文件异常
     */
    public static DocumentType detectDocumentType(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        // 使用FileMagic.prepareToCheckMagic自动处理流的mark/reset支持
        InputStream magicStream = FileMagic.prepareToCheckMagic(inputStream);
        FileMagic fileMagic = FileMagic.valueOf(magicStream);
        
        switch (fileMagic) {
            case OOXML:
                // OOXML格式（ZIP包），需要进一步检查是否为Word文档
                return isWordOOXMLFast(magicStream) ? DocumentType.DOCX : DocumentType.UNKNOWN;
                
            case OLE2:
                // OLE2格式，需要进一步检查是否为Word文档
                return isWordOLE2Fast(magicStream) ? DocumentType.DOC : DocumentType.UNKNOWN;
                
            default:
                return DocumentType.UNKNOWN;
        }
    }
    
    /**
     * 快速判断OOXML格式的文件是否为Word文档
     * 优化版本：只检查关键标识，遇到Word文档特征立即返回
     * @param inputStream 输入流
     * @return true如果是Word文档
     */
    private static boolean isWordOOXMLFast(InputStream inputStream) {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        
        inputStream.mark(Integer.MAX_VALUE);
        
        try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            
            // 检查ZIP包中的关键条目，只需要找到word/document.xml即可确定
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // Word文档的核心标识 - 找到即可立即返回
                if ("word/document.xml".equals(entryName)) {
                    return true;
                }
                
                // Excel的核心标识 - 如果先遇到Excel特征，立即返回false
                if ("xl/workbook.xml".equals(entryName) || "xl/sharedStrings.xml".equals(entryName)) {
                    return false;
                }
                
                // PowerPoint的核心标识 - 如果先遇到PPT特征，立即返回false
                if ("ppt/presentation.xml".equals(entryName) || "ppt/slides/slide1.xml".equals(entryName)) {
                    return false;
                }
                
                zipIn.closeEntry();
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        } finally {
            try {
                inputStream.reset();
            } catch (IOException ignored) {
                // 忽略reset异常
            }
        }
    }
    
    /**
     * 快速判断OLE2格式的文件是否为Word文档
     * 优化版本：只检查关键存储条目，避免完整解析
     * @param inputStream 输入流
     * @return true如果是Word文档
     */
    private static boolean isWordOLE2Fast(InputStream inputStream) {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        
        inputStream.mark(Integer.MAX_VALUE);
        
        try {
            // 使用POI的POIFSFileSystem来检查OLE2结构
            POIFSFileSystem poifs = new POIFSFileSystem(inputStream);
            
            // 优先检查Word文档的核心标识
            boolean hasWordDocument = poifs.getRoot().hasEntry("WordDocument");
            if (!hasWordDocument) {
                poifs.close();
                return false;
            }
            
            // 检查Excel的核心标识 - 如果有Excel特征，说明不是Word
            if (poifs.getRoot().hasEntry("Workbook") || poifs.getRoot().hasEntry("Book")) {
                poifs.close();
                return false;
            }
            
            // 检查PowerPoint的核心标识
            if (poifs.getRoot().hasEntry("PowerPoint Document")) {
                poifs.close();
                return false;
            }
            
            poifs.close();
            
            // 有WordDocument且没有其他Office应用的特征，确认为Word文档
            return true;
            
        } catch (Exception e) {
            return false;
        } finally {
            try {
                inputStream.reset();
            } catch (IOException ignored) {
                // 忽略reset异常
            }
        }
    }
    
    /**
     * 判断文件是否为Word文档
     * @param file 文件
     * @return true如果是Word文档
     * @throws IOException 读取文件异常
     */
    public static boolean isWordDocument(File file) throws IOException {
        return detectDocumentType(file) == DocumentType.DOC || detectDocumentType(file) == DocumentType.DOCX;
    }

    /**
     * 判断输入流是否为Word文档
     * 
     * <p><strong>注意：</strong>此方法不会关闭传入的InputStream，调用者负责资源管理</p>
     * 
     * @param inputStream 文件输入流（调用者负责关闭）
     * @return true如果是Word文档
     * @throws IOException 读取文件异常
     */
    public static boolean isWordDocument(InputStream inputStream) throws IOException {
        DocumentType type = detectDocumentType(inputStream);
        return type == DocumentType.DOC || type == DocumentType.DOCX;
    }
    
    
    // ================= HTML转换相关方法 =================
    
    /**
     * 将Word文档转换为HTML字符串
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream) throws Exception {
        return convertToHtml(inputStream, null, true, null);
    }
    
    /**
     * 将Word文档转换为HTML字符串
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 文件名（可选，用于设置HTML title）
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName) throws Exception {
        return convertToHtml(inputStream, fileName, true, null);
    }
    
    /**
     * 将Word文档转换为HTML字符串（基于文件）
     * 自动管理文件流的生命周期
     * @param file Word文件
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(File file) throws Exception {
        return convertToHtml(file, true, null);
    }
    
    /**
     * 将Word文档转换为HTML字符串（基于文件，可控制图片处理）
     * 自动管理文件流的生命周期
     * @param file Word文件
     * @param processImages 是否处理图片
     * @param imageHandler 图片处理器，为null时使用默认处理器
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(File file, boolean processImages, Function<PicInfo, String> imageHandler) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return convertToHtml(fis, file.getName(), processImages, imageHandler);
        }
    }
    
    /**
     * 将Word文档转换为HTML字符串（可控制图片处理）
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 文件名（可选，用于设置HTML title）
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
     * @param htmlFilePath 输出HTML文件路径
     * @throws Exception 转换或保存异常
     */
    public static void convertToHtmlFile(InputStream inputStream, String htmlFilePath) throws Exception {
        String html = convertToHtml(inputStream);
            
        // 保存到文件
        java.nio.file.Files.write(
            java.nio.file.Paths.get(htmlFilePath), 
            html.getBytes("UTF-8")
        );
    }
    
    /**
     * 将Word文档转换为HTML并保存到文件
     * @param inputStream Word文件输入流（必须支持mark/reset）
     * @param fileName 文件名（可选，用于设置HTML title）
     * @param htmlFilePath 输出HTML文件路径
     * @throws Exception 转换或保存异常
     */
    public static void convertToHtmlFile(InputStream inputStream, String fileName, String htmlFilePath) throws Exception {
        String html = convertToHtml(inputStream, fileName);
            
        // 保存到文件
        java.nio.file.Files.write(
            java.nio.file.Paths.get(htmlFilePath), 
            html.getBytes("UTF-8")
        );
    }
    
    /**
     * 将Word文档转换为HTML并保存到文件（基于文件）
     * 自动管理文件流的生命周期
     * @param file Word文件
     * @param htmlFilePath 输出HTML文件路径
     * @throws Exception 转换或保存异常
     */
    public static void convertToHtmlFile(File file, String htmlFilePath) throws Exception {
        String html = convertToHtml(file);
            
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
