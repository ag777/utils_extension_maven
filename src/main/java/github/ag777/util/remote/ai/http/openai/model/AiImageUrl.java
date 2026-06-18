package github.ag777.util.remote.ai.http.openai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * OpenAI兼容图片内容。
 *
 * <p>{@code url} 可以是公网图片地址，也可以是 {@code data:image/...;base64,...}。
 *
 * @author ag777
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true, fluent = true)
public class AiImageUrl {
    private String url;
    private String detail;

    /**
     * 创建图片内容。
     *
     * @param url 图片URL或data URL
     * @return 图片内容
     */
    public static AiImageUrl of(String url) {
        return new AiImageUrl(url, null);
    }

    /**
     * 创建图片内容。
     *
     * @param url 图片URL或data URL
     * @param detail 图片理解细节级别，可使用 {@link AiImageDetail}
     * @return 图片内容
     */
    public static AiImageUrl of(String url, String detail) {
        return new AiImageUrl(url, detail);
    }
}
