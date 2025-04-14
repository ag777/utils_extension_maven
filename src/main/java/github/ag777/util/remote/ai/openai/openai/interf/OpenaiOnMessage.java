package github.ag777.util.remote.ai.openai.openai.interf;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.model.AiTool;
import github.ag777.util.remote.ai.openai.openai.util.OpenaiResponseChatStreamUtil;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/23 上午9:36
 */
@FunctionalInterface
public interface OpenaiOnMessage {
    void accept(String message, AiTool tool, OpenaiResponseChatStreamUtil responseChatUtil) throws ValidateException, InterruptedException;
}
