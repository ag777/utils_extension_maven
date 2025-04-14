package github.ag777.util.remote.ai.openai.agent.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/27 下午2:41
 */
public class AiFunctionResultUtils {
    private static final Pattern PATTERN_JSON = Pattern.compile(
            "(?s)(?:```(?:json)?(.*?)```)",
            Pattern.CASE_INSENSITIVE
    );

    public static String getJson(String result) {
        if (result == null) {
            return null;
        }
        result = result.strip();
        Matcher m = PATTERN_JSON.matcher(result);
        if (m.find()) {
            return m.group(1);
        }
        return result;
    }
}
