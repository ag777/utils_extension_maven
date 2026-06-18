package github.ag777.util.remote.ai.http.openai.model;

import com.google.gson.annotations.SerializedName;
import github.ag777.util.remote.ai.http.openai.support.AiImageDataUrlUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * OpenAI兼容消息内容片段。
 *
 * <p>用于表达多模态消息中的文本、图片等结构化内容。
 *
 * @author ag777
 */
@NoArgsConstructor
@Data
public class AiMessageContentPart {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE_URL = "image_url";

    private String type;

    protected AiMessageContentPart(String type) {
        this.type = type;
    }

    /**
     * 创建文本内容片段。
     *
     * @param text 文本内容
     * @return 文本内容片段
     */
    public static AiMessageContentPart text(String text) {
        return new TextPart(text);
    }

    /**
     * 创建图片内容片段。
     *
     * @param url 图片URL或data URL
     * @return 图片内容片段
     */
    public static AiMessageContentPart imageUrl(String url) {
        return imageUrl(AiImageUrl.of(url));
    }

    /**
     * 创建图片内容片段。
     *
     * @param url 图片URL或data URL
     * @param detail 图片理解细节级别，可使用 {@link AiImageDetail}
     * @return 图片内容片段
     */
    public static AiMessageContentPart imageUrl(String url, String detail) {
        return imageUrl(AiImageUrl.of(url, detail));
    }

    /**
     * 创建图片内容片段。
     *
     * @param imageUrl 图片内容
     * @return 图片内容片段
     */
    public static AiMessageContentPart imageUrl(AiImageUrl imageUrl) {
        return new ImageUrlPart(imageUrl);
    }

    /**
     * 创建本地图片内容片段。
     *
     * @param file 本地图片文件
     * @return 图片内容片段
     * @throws IOException 读取文件失败
     */
    public static AiMessageContentPart imageFile(File file) throws IOException {
        return imageFile(file, null);
    }

    /**
     * 创建本地图片内容片段。
     *
     * @param path 本地图片路径
     * @return 图片内容片段
     * @throws IOException 读取文件失败
     */
    public static AiMessageContentPart imageFile(Path path) throws IOException {
        return imageFile(path, null);
    }

    /**
     * 创建本地图片内容片段。
     *
     * @param file 本地图片文件
     * @param detail 图片理解细节级别，可使用 {@link AiImageDetail}
     * @return 图片内容片段
     * @throws IOException 读取文件失败
     */
    public static AiMessageContentPart imageFile(File file, String detail) throws IOException {
        return imageUrl(AiImageDataUrlUtils.toDataUrl(file), detail);
    }

    /**
     * 创建本地图片内容片段。
     *
     * @param path 本地图片路径
     * @param detail 图片理解细节级别，可使用 {@link AiImageDetail}
     * @return 图片内容片段
     * @throws IOException 读取文件失败
     */
    public static AiMessageContentPart imageFile(Path path, String detail) throws IOException {
        return imageUrl(AiImageDataUrlUtils.toDataUrl(path), detail);
    }

    /**
     * 文本内容片段。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TextPart extends AiMessageContentPart {
        private String text;

        public TextPart() {
            super(TYPE_TEXT);
        }

        public TextPart(String text) {
            super(TYPE_TEXT);
            this.text = text;
        }
    }

    /**
     * 图片内容片段。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ImageUrlPart extends AiMessageContentPart {
        @SerializedName("image_url")
        private AiImageUrl imageUrl;

        public ImageUrlPart() {
            super(TYPE_IMAGE_URL);
        }

        public ImageUrlPart(AiImageUrl imageUrl) {
            super(TYPE_IMAGE_URL);
            this.imageUrl = imageUrl;
        }
    }
}
