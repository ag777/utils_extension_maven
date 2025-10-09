package github.ag777.util.file.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description PdfBox库二次封装
 * @Date 2022/1/13 9:00
 */
public class PdfBoxUtils {

    public static final String TYPE_PNG = "png";


    /**
     * PDF文件转PNG图片，全部页数
     *
     * @param pdfFile  pdf完整路径
     * @param outputBaseDir 图片存放的文件夹
     * @param imgFormatName 输出图片类型
     * @param dpi dpi越大转换后越清晰，相对转换速度越慢
     * @return 返回转换后图片集合list,拆分的图片会放在outputBaseDir下以0.xxx, 1.xxx...来命名
     */
    public static List<File> toImage(File pdfFile, File outputBaseDir, String imgFormatName, int dpi) throws IOException {
        if (!outputBaseDir.exists()) {
            outputBaseDir.mkdirs();
        }
        PDDocument doc = Loader.loadPDF(pdfFile);
        //String imagePDFName = srcFile.getName().substring(0, dot); // 获取图片文件名
        PDFRenderer renderer = new PDFRenderer(doc);
        String dirPath = outputBaseDir.getAbsolutePath() + File.separator;
        int pageCount = doc.getNumberOfPages();
        List<File> fileList = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, dpi); // 162.8
            File outFile = new File(dirPath + i + "." + imgFormatName);
            ImageIO.write(image, imgFormatName, outFile);
            fileList.add(outFile);
        }

        return fileList;

    }

    /**
     * 将若干张图片合并为一个PDF（A4竖版），一图一页；
     * 若图片为横向（宽>高）则自动顺时针旋转90度，使图片长边与PDF长边对齐；
     * 图片按页面可用区域等比缩放，且水平/垂直居中。
     *
     * @param imageFiles   图片文件列表（按顺序一图一页）
     * @param outputPdf    输出PDF文件路径
     * @param scaleRatio   图片缩放比例(0,1]
     * @return 输出PDF文件
     */
    public static File toPdf(List<File> imageFiles, File outputPdf, float scaleRatio) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("imageFiles 不能为空");
        }
        File parent = outputPdf.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        final PDRectangle pageSize = PDRectangle.A4; // 竖版A4
        final float pageWidth = pageSize.getWidth();
        final float pageHeight = pageSize.getHeight();
        final float maxDrawWidth = pageWidth * scaleRatio;
        final float maxDrawHeight = pageHeight * scaleRatio;

        try (PDDocument document = new PDDocument()) {
            for (File imageFile : imageFiles) {
                BufferedImage src = ImageIO.read(imageFile);
                if (src == null) {
                    // 跳过无法读取的图片
                    continue;
                }

                // 横向图片旋转90度
                BufferedImage processed = src.getWidth() > src.getHeight() ? rotate90Clockwise(src) : src;

                float imgW = processed.getWidth();
                float imgH = processed.getHeight();

                // 等比缩放以适配80%页面区域
                float scale = Math.min(maxDrawWidth / imgW, maxDrawHeight / imgH);
                float drawW = imgW * scale;
                float drawH = imgH * scale;
                float x = (pageWidth - drawW) / 2f;
                float y = (pageHeight - drawH) / 2f;

                PDPage page = new PDPage(pageSize);
                document.addPage(page);

                PDImageXObject pdImage = LosslessFactory.createFromImage(document, processed);
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, x, y, drawW, drawH);
                }
            }

            document.save(outputPdf);
        }

        return outputPdf;
    }

    // 顺时针旋转90度
    private static BufferedImage rotate90Clockwise(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dst.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        AffineTransform at = new AffineTransform();
        at.translate(h, 0);
        at.rotate(Math.toRadians(90));
        g2d.drawImage(src, at, null);
        g2d.dispose();
        return dst;
    }

    public static void main(String[] args) throws IOException {
        toImage(
                new File("D:\\a.pdf"),
                new File("D:\\temp\\"),
                TYPE_PNG,
                144);
    }
    
}
