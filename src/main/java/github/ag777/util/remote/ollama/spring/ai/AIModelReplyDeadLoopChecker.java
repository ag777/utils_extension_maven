package github.ag777.util.remote.ollama.spring.ai;

import github.ag777.util.remote.ollama.spring.ai.model.AIModelReplyDeadLoopException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/5/31 下午3:48
 */
public class AIModelReplyDeadLoopChecker {
    // 重复判断
    private final static Pattern P_REPEAT = Pattern.compile("(.+?\\S+.*)\\1{9}$", Pattern.DOTALL);

    /**
     * 测试给定的StringBuilder对象是否包含超过指定阈值长度的重复模式。
     * 如果发现重复模式，将抛出AIModelReplyDeadLoopException异常。
     *
     * @param sb 要检查的StringBuilder对象。
     * @param thresholdLength 阈值长度，用于判断StringBuilder长度是否超过限制。
     * @throws AIModelReplyDeadLoopException 如果发现重复模式，且该模式长度超过阈值，抛出此异常。
     */
    public static void test(StringBuilder sb, int thresholdLength) throws AIModelReplyDeadLoopException {
        // 检查StringBuilder的长度是否超过阈值长度
        if (sb.length()>thresholdLength) {
            // 使用预定义的正则表达式模式搜索重复模式
            Matcher m = P_REPEAT.matcher(sb);
            if (m.find()) {
                throw new AIModelReplyDeadLoopException(sb.toString(), m.group(1));
            }
        }
    }
}
