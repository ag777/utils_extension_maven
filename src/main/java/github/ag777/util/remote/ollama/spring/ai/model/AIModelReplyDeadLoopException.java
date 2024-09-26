package github.ag777.util.remote.ollama.spring.ai.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ai大模型回复死循环
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/5/31 下午3:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AIModelReplyDeadLoopException extends Exception {
    private String reply;
    private String repeatText;
    public AIModelReplyDeadLoopException(String reply, String repeatText) {
        this.reply = reply;
        this.repeatText = repeatText;
    }
}
