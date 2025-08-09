package github.ag777.util.software.oof;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 115网盘工具类
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/8/3 14:20
 */
public class OofUtils {
    
    /**
     * 从cookie字符串中提取userId
     * cookie格式示例: UID=10445976_A1_1754189694
     * 
     * @param cookie cookie字符串
     * @return 提取到的userId，如果未找到返回Optional.empty()
     */
    public static Optional<Long> extractUserIdFromCookie(String cookie) {
        if (cookie == null || cookie.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // 正则表达式：匹配UID=数字_其他内容
        // 解释：
        // UID= - 字面匹配
        // (\d+) - 捕获组1：一个或多个数字（userId）
        // _ - 字面匹配下划线
        // [^;]* - 匹配分号前的任何字符（可选）
        Pattern pattern = Pattern.compile("UID=(\\d+)_[^;]*");
        Matcher matcher = pattern.matcher(cookie);
        
        if (matcher.find()) {
            try {
                String userIdStr = matcher.group(1);
                return Optional.of(Long.parseLong(userIdStr));
            } catch (NumberFormatException e) {
                // 如果数字解析失败，返回空
                return Optional.empty();
            }
        }
        
        return Optional.empty();
    }
    
}
