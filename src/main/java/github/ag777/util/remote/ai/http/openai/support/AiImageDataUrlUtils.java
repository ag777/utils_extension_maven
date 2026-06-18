package github.ag777.util.remote.ai.http.openai.support;

import github.ag777.util.file.FileUtils;
import github.ag777.util.lang.security.Base64Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * 本地图片转 OpenAI 兼容 data URL 工具。
 *
 * @author ag777
 */
public final class AiImageDataUrlUtils {
    private static final Map<String, String> EXT_TO_MIME = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif"
    );

    private AiImageDataUrlUtils() {
    }

    /**
     * 将图片字节编码为 data URL。
     *
     * @param bytes 图片字节
     * @param mimeType MIME 类型，如 image/jpeg
     * @return data URL
     */
    public static String toDataUrl(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("图片内容不能为空");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType不能为空");
        }
        String base64 = new String(Base64Utils.encode(bytes), StandardCharsets.US_ASCII);
        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * 将本地图片文件编码为 data URL。
     *
     * @param file 图片文件
     * @return data URL
     * @throws IOException 读取文件失败
     */
    public static String toDataUrl(File file) throws IOException {
        return toDataUrl(file.toPath());
    }

    /**
     * 将本地图片文件编码为 data URL。
     *
     * @param path 图片路径
     * @return data URL
     * @throws IOException 读取文件失败
     */
    public static String toDataUrl(Path path) throws IOException {
        return toDataUrl(path, guessImageMimeType(path));
    }

    /**
     * 将本地图片文件编码为 data URL。
     *
     * @param path 图片路径
     * @param mimeType MIME 类型
     * @return data URL
     * @throws IOException 读取文件失败
     */
    public static String toDataUrl(Path path, String mimeType) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path不能为空");
        }
        return toDataUrl(FileUtils.readBytes(path.toFile()), mimeType);
    }

    /**
     * 根据文件扩展名推断图片 MIME 类型。
     *
     * @param path 图片路径
     * @return MIME 类型
     */
    public static String guessImageMimeType(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path不能为空");
        }
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("无法从文件名推断图片类型: " + fileName);
        }
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        String mimeType = EXT_TO_MIME.get(ext);
        if (mimeType == null) {
            throw new IllegalArgumentException("不支持的图片扩展名: " + ext);
        }
        return mimeType;
    }
}
