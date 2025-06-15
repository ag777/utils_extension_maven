package github.ag777.util.remote.ai.openai.http.interf;

import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.http.util.OpenaiResponseChatStreamUtil;
import github.ag777.util.remote.ai.openai.model.AiTool;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/23 上午9:36
 */
@FunctionalInterface
public interface OpenaiOnMessage {
    void accept(String message, AiTool tool, OpenaiResponseChatStreamUtil responseChatUtil) throws ValidateException, InterruptedException;
}
