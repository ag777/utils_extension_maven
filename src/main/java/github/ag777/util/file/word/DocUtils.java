package github.ag777.util.file.word;

import github.ag777.util.file.word.model.PicInfo;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * DOC文档工具类
 * 基于Apache POI的HWPF API实现
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2025年09月11日,last modify at 2025年09月12日
 */
public class DocUtils {
    
    /**
     * 将DOC文档直接转换为HTML字符串
     * @param inputStream DOC文件输入流
     * @param fileName 文件名
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName) throws Exception {
        return convertToHtml(inputStream, fileName, true, null);
    }
    
    /**
     * 将DOC文档直接转换为HTML字符串（可控制图片处理）
     * @param inputStream DOC文件输入流
     * @param fileName 文件名
     * @param processImages 是否处理图片
     * @param imageHandler 图片处理器，为null时使用默认处理器
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName, 
                                     boolean processImages, Function<PicInfo, String> imageHandler) throws Exception {
        try (HWPFDocument document = new HWPFDocument(inputStream)) {
            StringBuilder html = new StringBuilder();
            
            // HTML文档头部
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"zh-CN\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>").append(escapeHtml(fileName != null ? fileName : "Document")).append("</title>\n");
            html.append("    <style>\n");
            html.append(getDefaultCss());
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            
            // 解析文档内容
            parseDocContentToHtml(document, html, processImages, imageHandler);
            
            html.append("</body>\n");
            html.append("</html>");
            
            return html.toString();
        }
    }
    
    /**
     * 获取默认CSS样式
     */
    private static String getDefaultCss() {
        return """
            body {
                font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
                line-height: 1.6;
                margin: 40px;
                background-color: #fff;
                color: #333;
            }
            h1, h2, h3, h4, h5, h6 {
                color: #2c3e50;
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
            }
            h1 { font-size: 2em; border-bottom: 2px solid #eee; padding-bottom: 8px; }
            h2 { font-size: 1.5em; border-bottom: 1px solid #eee; padding-bottom: 4px; }
            h3 { font-size: 1.3em; }
            h4 { font-size: 1.1em; }
            h5 { font-size: 1em; }
            h6 { font-size: 0.9em; }
            p {
                margin: 12px 0;
                text-align: justify;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 16px 0;
                border: 1px solid #ddd;
            }
            th, td {
                border: 1px solid #ddd;
                padding: 8px 12px;
                text-align: left;
                vertical-align: top;
            }
            th {
                background-color: #f5f5f5;
                font-weight: 600;
            }
            tr:nth-child(even) {
                background-color: #f9f9f9;
            }
            .table-cell-content {
                margin: 0;
                padding: 0;
            }
            img {
                max-width: 100%;
                height: auto;
                margin: 12px 0;
                border: 1px solid #ddd;
                border-radius: 4px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .image-container {
                text-align: center;
                margin: 16px 0;
            }
            .image-caption {
                font-size: 0.9em;
                color: #666;
                font-style: italic;
                margin-top: 8px;
            }
            """;
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
    
    /**
     * 解析DOC文档内容为HTML
     */
    private static void parseDocContentToHtml(HWPFDocument document, StringBuilder html, 
                                            boolean processImages, Function<PicInfo, String> imageHandler) {
        Range range = document.getRange();
        List<Block> blocks = collectBlocks(range);
        
        // 获取图片信息
        List<Picture> pictures = null;
        if (processImages) {
            PicturesTable picturesTable = document.getPicturesTable();
            pictures = picturesTable.getAllPictures();
        }
        
        // 按偏移量排序，获得文档真实顺序
        blocks.sort(Comparator.comparingInt(b -> b.start));
        
        for (Block block : blocks) {
            if ("paragraph".equals(block.type)) {
                Paragraph paragraph = (Paragraph) block.ref;
                convertDocParagraphToHtml(paragraph, html, processImages, pictures, imageHandler);
            } else if ("table".equals(block.type)) {
                Table table = (Table) block.ref;
                convertTableToHtml(table, html);
            }
        }
        
        // 如果有未处理的图片，在文档末尾添加
        if (processImages && pictures != null) {
            addRemainingPictures(pictures, html, imageHandler);
        }
    }
    
    /**
     * 将DOC段落转换为HTML（支持图片）
     */
    private static void convertDocParagraphToHtml(Paragraph paragraph, StringBuilder html, 
                                                boolean processImages, List<Picture> pictures, 
                                                Function<PicInfo, String> imageHandler) {
        String text = paragraph.text().trim();
        boolean hasImages = false;
        
        // 检查段落中是否包含图片字符
        if (processImages && text.contains("\u0001") && pictures != null && !pictures.isEmpty()) {
            // 处理包含图片的段落
            String[] parts = text.split("\u0001");
            StringBuilder content = new StringBuilder();
            
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (!part.isEmpty()) {
                    content.append(escapeHtml(part));
                }
                
                // 在每个分割点尝试插入图片
                if (i < parts.length - 1 && !pictures.isEmpty()) {
                    Picture picture = pictures.remove(0); // 按顺序取出图片
                    String imageHtml = convertDocPictureToHtml(picture, imageHandler);
                    if (!imageHtml.isEmpty()) {
                        content.append(imageHtml);
                        hasImages = true;
                    }
                }
            }
            
            if (content.length() > 0) {
                if (hasImages) {
                    html.append("    <div class=\"image-container\">\n");
                    html.append("        ").append(content).append("\n");
                    html.append("    </div>\n");
                } else {
                    outputDocParagraph(content.toString(), text, html);
                }
            }
        } else if (!text.isEmpty()) {
            // 普通段落
            outputDocParagraph(escapeHtml(text), text, html);
        }
    }
    
    /**
     * 输出DOC段落HTML
     */
    private static void outputDocParagraph(String content, String originalText, StringBuilder html) {
        int headingLevel = detectHeadingLevel(originalText);
        if (headingLevel > 0) {
            html.append("    <h").append(headingLevel).append(">")
                .append(content)
                .append("</h").append(headingLevel).append(">\n");
        } else {
            html.append("    <p>").append(content).append("</p>\n");
        }
    }
    
    /**
     * 将DOC图片转换为HTML
     */
    private static String convertDocPictureToHtml(Picture picture, Function<PicInfo, String> imageHandler) {
        try {
            byte[] imageBytes = picture.getContent();
            
            if (imageBytes == null || imageBytes.length == 0) {
                return "<p><em>[图片无法显示]</em></p>";
            }
            
            // 获取图片格式
            String mimeType = picture.getMimeType();
            if (mimeType == null || mimeType.isEmpty()) {
                // 根据文件扩展名推断MIME类型
                String ext = picture.suggestFileExtension();
                mimeType = getMimeTypeFromExtension(ext);
            }
            
            // 创建PicInfo对象
            PicInfo picInfo = new PicInfo(imageBytes, mimeType, null);
            
            // 使用图片处理器处理
            if (imageHandler != null) {
                return imageHandler.apply(picInfo);
            } else {
                // 返回空
                return "<br/>";
            }
            
        } catch (Exception e) {
            System.err.println("处理DOC图片时出错: " + e.getMessage());
            return "<p><em>[图片处理失败: " + escapeHtml(e.getMessage()) + "]</em></p>";
        }
    }
    
    /**
     * 根据文件扩展名获取MIME类型
     */
    private static String getMimeTypeFromExtension(String ext) {
        if (ext == null) return "image/png";
        switch (ext.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                return "image/png";
        }
    }
    
    /**
     * 添加剩余的图片到HTML末尾
     */
    private static void addRemainingPictures(List<Picture> pictures, StringBuilder html, 
                                           Function<PicInfo, String> imageHandler) {
        if (!pictures.isEmpty()) {
            html.append("    <div class=\"image-container\">\n");
            html.append("        <h3>文档中的其他图片：</h3>\n");
            for (Picture picture : pictures) {
                String imageHtml = convertDocPictureToHtml(picture, imageHandler);
                if (!imageHtml.isEmpty()) {
                    html.append("        ").append(imageHtml).append("\n");
                }
            }
            html.append("    </div>\n");
        }
    }
    
    /**
     * 将表格转换为HTML
     */
    private static void convertTableToHtml(Table table, StringBuilder html) {
        html.append("    <table>\n");
        
        for (int r = 0; r < table.numRows(); r++) {
            TableRow row = table.getRow(r);
            html.append("        <tr>\n");
            
            for (int c = 0; c < row.numCells(); c++) {
                TableCell cell = row.getCell(c);
                String cellText = cell.text();
                if (cellText != null) {
                    cellText = cellText.replace('\r', ' ').trim();
                }
                
                // 第一行作为表头
                String tag = (r == 0) ? "th" : "td";
                html.append("            <").append(tag).append(">")
                    .append(escapeHtml(cellText != null ? cellText : ""))
                    .append("</").append(tag).append(">\n");
            }
            
            html.append("        </tr>\n");
        }
        
        html.append("    </table>\n");
    }
    
    /**
     * 收集文档中的所有块（段落块和表格块）
     */
    private static List<Block> collectBlocks(Range range) {
        List<Block> blocks = new ArrayList<>();
        
        // 收集表格块
        TableIterator tableIterator = new TableIterator(range);
        while (tableIterator.hasNext()) {
            Table table = tableIterator.next();
            blocks.add(new Block(table.getStartOffset(), table.getEndOffset(), "table", table));
        }
        
        // 收集非表格段落块
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph paragraph = range.getParagraph(i);
            if (!paragraph.isInTable()) {
                blocks.add(new Block(paragraph.getStartOffset(), paragraph.getEndOffset(), "paragraph", paragraph));
            }
        }
        
        return blocks;
    }
    
    /**
     * 通过文本特征检测标题级别（适用于DOC格式）
     */
    private static int detectHeadingLevel(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 检测数字编号的标题，如 "1. 标题", "1.1 子标题", "1.1.1 子子标题"
        if (text.matches("^\\d+\\..*")) {
            return 1;
        }
        if (text.matches("^\\d+\\.\\d+\\..*")) {
            return 2;
        }
        if (text.matches("^\\d+\\.\\d+\\.\\d+\\..*")) {
            return 3;
        }
        
        // 检测中文编号，如 "一、", "（一）", "1、"
        if (text.matches("^[一二三四五六七八九十]+、.*")) {
            return 1;
        }
        if (text.matches("^（[一二三四五六七八九十]+）.*")) {
            return 2;
        }
        if (text.matches("^\\d+、.*")) {
            return 1;
        }
        
        // 检测短文本且以冒号结尾的可能是标题
        if (text.length() < 50 && text.endsWith("：")) {
            return 1;
        }
        
        return 0; // 不是标题
    }
    
    /**
     * 文档块，用于排序重建文档顺序
     */
    private static class Block {
        final int start;
        @SuppressWarnings("unused") // 保留用于未来扩展
        final int end;
        final String type;
        final Object ref;
        
        Block(int start, int end, String type, Object ref) {
            this.start = start;
            this.end = end;
            this.type = type;
            this.ref = ref;
        }
    }
}