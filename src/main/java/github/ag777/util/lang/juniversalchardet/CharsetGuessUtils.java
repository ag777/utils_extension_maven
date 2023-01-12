package github.ag777.util.lang.juniversalchardet;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * 文件编码识别工具，对juniversalchardet的二次封装
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/1/12 15:46
 */
public class CharsetGuessUtils {

    /**
     *
     * @param filePath 文件路径
     * @return 文件编码
     * @throws IOException 读取文件异常
     */
    public static Optional<String> guess(String filePath) throws IOException {
        InputStream fis = Files.newInputStream(Paths.get(filePath));
        return guess(fis);
    }

    /**
     *
     * @param in 输入流
     * @return 文件编码
     * @throws IOException io操作异常
     */
    public static Optional<String> guess(InputStream in) throws IOException {
        byte[] buf = new byte[4096];
        // (1)
        UniversalDetector detector = new UniversalDetector();

        try {
            // (2)
            int nread;
            while ((nread = in.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();

            // (4)
            String encoding = detector.getDetectedCharset();
            return Optional.ofNullable(encoding);
        } finally {
            // (5)
            detector.reset();
        }
    }

    /**
     *
     * @param bytes 字节数组
     * @return io操作异常
     */
    public static Optional<String> guess(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector();
        try {
            // (2)
            detector.handleData(bytes, 0, bytes.length);
            // (3)
            detector.dataEnd();

            // (4)
            String encoding = detector.getDetectedCharset();
            return Optional.ofNullable(encoding);
        } finally {
            // (5)
            detector.reset();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(guess("D:\\temp\\程序测试\\ddzsBase.properties").get());
    }
}
