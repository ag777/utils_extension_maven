package github.ag777.util.file.word;

import github.ag777.util.file.word.model.PicInfo;
import org.apache.poi.xwpf.usermodel.*;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

/**
 * DOCX文档工具类
 * 基于Apache POI的XWPF API实现
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2025年09月11日,last modify at 2025年09月12日
 */
public class DocxUtils {
    
    /**
     * 将DOCX文档直接转换为HTML字符串
     * @param inputStream DOCX文件输入流
     * @param fileName 文件名
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName) throws Exception {
        return convertToHtml(inputStream, fileName, true, null);
    }
    
    /**
     * 将DOCX文档直接转换为HTML字符串（可控制图片处理）
     * @param inputStream DOCX文件输入流
     * @param fileName 文件名
     * @param processImages 是否处理图片
     * @param imageHandler 图片处理器，为null时使用默认处理器
     * @return HTML字符串
     * @throws Exception 转换异常
     */
    public static String convertToHtml(InputStream inputStream, String fileName, 
                                     boolean processImages, Function<PicInfo, String> imageHandler) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
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
            parseDocxContentToHtml(document, html, processImages, imageHandler);
            
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
     * 解析DOCX文档内容为HTML
     */
    private static void parseDocxContentToHtml(XWPFDocument document, StringBuilder html, 
                                             boolean processImages, Function<PicInfo, String> imageHandler) {
        for (IBodyElement bodyElement : document.getBodyElements()) {
            if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                convertParagraphToHtml(paragraph, html, processImages, imageHandler);
            } else if (bodyElement.getElementType() == BodyElementType.TABLE) {
                XWPFTable table = (XWPFTable) bodyElement;
                convertTableToHtml(table, html, processImages, imageHandler);
            }
        }
    }
    
    /**
     * 将段落转换为HTML
     */
    private static void convertParagraphToHtml(XWPFParagraph paragraph, StringBuilder html, 
                                             boolean processImages, Function<PicInfo, String> imageHandler) {
        String style = paragraph.getStyle();
        int headingLevel = getHeadingLevel(style);
        
        // 构建段落内容，处理格式化和图片
        StringBuilder paragraphContent = new StringBuilder();
        boolean hasImages = false;
        
        for (XWPFRun run : paragraph.getRuns()) {
            // 处理文本
            String runText = run.text();
            if (runText != null && !runText.isEmpty()) {
                String formattedText = applyTextFormatting(runText, run);
                paragraphContent.append(formattedText);
            }
            
            // 处理图片
            if (processImages) {
                List<XWPFPicture> pictures = run.getEmbeddedPictures();
                for (XWPFPicture picture : pictures) {
                    String imageHtml = convertPictureToHtml(picture, imageHandler);
                    if (!imageHtml.isEmpty()) {
                        paragraphContent.append(imageHtml);
                        hasImages = true;
                    }
                }
            }
        }
        
        String content = paragraphContent.toString().trim();
        if (!content.isEmpty()) {
            if (hasImages) {
                // 如果包含图片，使用div包装以获得更好的布局
                html.append("    <div class=\"image-container\">\n");
                if (headingLevel > 0) {
                    html.append("        <h").append(headingLevel).append(">")
                        .append(content)
                        .append("</h").append(headingLevel).append(">\n");
                } else {
                    html.append("        ").append(content).append("\n");
                }
                html.append("    </div>\n");
            } else {
                // 纯文本段落
                if (headingLevel > 0) {
                    html.append("    <h").append(headingLevel).append(">")
                        .append(content)
                        .append("</h").append(headingLevel).append(">\n");
                } else {
                    html.append("    <p>").append(content).append("</p>\n");
                }
            }
        }
    }
    
    /**
     * 应用文本格式化
     */
    private static String applyTextFormatting(String text, XWPFRun run) {
        String result = escapeHtml(text);
        
        // 粗体
        if (run.isBold()) {
            result = "<strong>" + result + "</strong>";
        }
        
        // 斜体
        if (run.isItalic()) {
            result = "<em>" + result + "</em>";
        }
        
        // 下划线
        if (run.getUnderline() != UnderlinePatterns.NONE) {
            result = "<u>" + result + "</u>";
        }
        
        return result;
    }
    
    /**
     * 将图片转换为HTML
     */
    private static String convertPictureToHtml(XWPFPicture picture, Function<PicInfo, String> imageHandler) {
        try {
            // 获取图片数据
            XWPFPictureData pictureData = picture.getPictureData();
            byte[] imageBytes = pictureData.getData();
            
            if (imageBytes == null || imageBytes.length == 0) {
                return "<p><em>[图片无法显示]</em></p>";
            }
            
            // 获取图片格式
            String mimeType = pictureData.getPackagePart().getContentType();
            String fileName = pictureData.getFileName();
            
            // 创建PicInfo对象
            PicInfo picInfo = new PicInfo(imageBytes, mimeType, fileName);
            
            // 使用图片处理器处理
            if (imageHandler != null) {
                return imageHandler.apply(picInfo);
            } else {
                // 返回空
                return "<br/>";
            }
            
        } catch (Exception e) {
            System.err.println("处理图片时出错: " + e.getMessage());
            return "<p><em>[图片处理失败: " + escapeHtml(e.getMessage()) + "]</em></p>";
        }
    }

    /**
     * 将表格转换为HTML
     */
    private static void convertTableToHtml(XWPFTable table, StringBuilder html, 
                                         boolean processImages, Function<PicInfo, String> imageHandler) {
        html.append("    <table>\n");
        
        List<XWPFTableRow> rows = table.getRows();
        for (int r = 0; r < rows.size(); r++) {
            XWPFTableRow row = rows.get(r);
            html.append("        <tr>\n");
            
            List<XWPFTableCell> cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                // 第一行作为表头
                String tag = (r == 0) ? "th" : "td";
                html.append("            <").append(tag).append(">");
                
                // 合并单元格内所有段落的文本
                StringBuilder cellContent = new StringBuilder();
                for (XWPFParagraph cellPara : cell.getParagraphs()) {
                    StringBuilder paraContent = new StringBuilder();
                    for (XWPFRun run : cellPara.getRuns()) {
                        String runText = run.text();
                        if (runText != null && !runText.isEmpty()) {
                            paraContent.append(applyTextFormatting(runText, run));
                        }
                        
                        // 处理表格中的图片
                        if (processImages) {
                            List<XWPFPicture> pictures = run.getEmbeddedPictures();
                            for (XWPFPicture picture : pictures) {
                                String imageHtml = convertPictureToHtml(picture, imageHandler);
                                if (!imageHtml.isEmpty()) {
                                    paraContent.append(imageHtml);
                                }
                            }
                        }
                    }
                    
                    String paraText = paraContent.toString().trim();
                    if (!paraText.isEmpty()) {
                        if (cellContent.length() > 0) {
                            cellContent.append("<br>");
                        }
                        cellContent.append(paraText);
                    }
                }
                
                html.append(cellContent.toString());
                html.append("</").append(tag).append(">\n");
            }
            
            html.append("        </tr>\n");
        }
        
        html.append("    </table>\n");
    }
    
    /**
     * 从样式名称中提取标题级别
     */
    private static int getHeadingLevel(String style) {
        if (style == null) {
            return 0;
        }
        
        String lowerStyle = style.toLowerCase();
        
        // 检测标准的Heading样式
        if (lowerStyle.contains("heading")) {
            try {
                // 提取数字，如 "Heading 1" -> 1
                String[] parts = style.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        int level = Integer.parseInt(part);
                        return level >= 1 && level <= 6 ? level : 0;
                    }
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 检测其他可能的标题样式
        if (lowerStyle.contains("title") || lowerStyle.startsWith("h")) {
            return 1; // 默认为1级标题
        }
        
        return 0; // 普通段落
    }
}