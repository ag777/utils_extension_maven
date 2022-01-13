package github.ag777.util.file.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
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

    public static void main(String[] args) throws IOException {
        toImage(
                new File("D:\\a.pdf"),
                new File("D:\\temp\\"),
                TYPE_PNG,
                144);
    }
    
}
