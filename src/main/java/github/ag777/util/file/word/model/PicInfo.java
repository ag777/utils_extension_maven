package github.ag777.util.file.word.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图片信息封装类
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2025年09月11日,last modify at 2025年09月12日
 */
@Data
@AllArgsConstructor
public class PicInfo {

    private final byte[] data;
    private final String mimeType;
    private final String fileName;

    /**
     * 判断图片数据是否有效
     * @return true如果图片数据不为空
     */
    public boolean isValid() {
        return data != null && data.length > 0;
    }

    /**
     * 获取图片大小（字节）
     * @return 图片大小
     */
    public int getSize() {
        return data != null ? data.length : 0;
    }
}
