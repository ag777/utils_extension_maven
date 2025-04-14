package github.ag777.util.remote.ai.openai.util;

import com.ag777.util.lang.ObjectUtils;
import github.ag777.util.remote.ai.openai.model.AiReply;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/27 上午8:59
 */
public class AiReplyUtils {
    private static final Pattern PATTERN_THINKING = Pattern.compile(
            "(?s)(?:<think>(.*?)</think>(.*)|(.*))$",
            Pattern.CASE_INSENSITIVE
    );

    public static void main(String[] args) {
//        String[] texts = new String[]{
//                "<think></think>aasd",
//                "<think>1\n23</think>aasd",
//                "<think><think></think>aasd",
//                "</think>aasd",
//                "<think>asd",
//                "as\nd",
//                "<think>ee</think>",
//                "",
//                "123<think>456</think>789"
//        };
//        for (String text : texts) {
//            Matcher m = PATTERN_THINKING.matcher(text);
//            if (m.find()) {
//                System.out.println(text);
//                // Group 1 is the thinking part (if valid tags are present)
//                // Group 2 is the text part (if valid tags are present)
//                // Group 3 is the entire text (if valid tags are not present)
//                String thinking = m.group(1);
//                String content = m.group(2);
//
//                // If group 1 and 2 are null, use group 3 as the content
//                if (thinking == null && content == null) {
//                    content = m.group(3);
//                }
//
//                System.out.println("思考: " + (thinking != null ? thinking : "null"));
//                System.out.println("正文: " + (content != null ? content : ""));
//                System.out.println();
//            } else {
//                System.out.println(text);
//                System.out.println("思考: null");
//                System.out.println("正文: \"\"");
//                System.out.println();
//            }
//        }
        handle("""
                Thought: To provide the current weather for Fuzhou, I need to fetch the latest weather data.
                                
                Action:
                ```
                {
                  "action": "weather",
                  "action_input": {
                    "city": "Fuzhou",
                    "date": "today"
                  }
                }
                ```
                """);
    }

    /**
     * 解析文本中的思考部分和正文部分
     * @param text 需要解析的文本
     * @return 解析后的 {@link AiReply} 对象，如果文本为空或没有找到有效的思考标签则返回null
     */
    public static AiReply handle(String text) {
        if (text == null) {
            return null;
        }
        
        Matcher m = PATTERN_THINKING.matcher(text);
        if (m.find()) {
            return new AiReply(m.group(1), ObjectUtils.getOrDefault(m.group(2), m.group(3)));
        }
        return null;
    }

    /**
     * 获取文本中的思考部分（<think></think>标签之间的内容）
     * 
     * @param text 需要解析的文本
     * @return 思考部分的内容，如果没有找到有效的思考标签则返回null
     */
    public static String getThinking(String text) {
        if (text == null) {
            return null;
        }
        
        Matcher m = PATTERN_THINKING.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    /**
     * 获取文本中的正文部分（<think></think>标签之外的内容）
     * 
     * @param text 需要解析的文本
     * @return 正文部分的内容，如果只有思考部分则返回空字符串，如果没有找到有效的思考标签则返回原文本
     */
    public static String getText(String text) {
        if (text == null) {
            return null;
        }
        
        Matcher m = PATTERN_THINKING.matcher(text);
        if (m.find()) {
            // 检查第二组（正文部分）是否存在
            String content = m.group(2);
            // 如果第一组和第二组都是null，则使用第三组作为内容
            if (m.group(1) == null && content == null) {
                content = m.group(3);
            }
            return content != null ? content.strip() : "";
        }
        return text.strip();
    }
    
}
