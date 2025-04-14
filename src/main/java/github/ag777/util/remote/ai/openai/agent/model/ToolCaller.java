package github.ag777.util.remote.ai.openai.agent.model;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.model.AiTool;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/8 下午3:54
 */
@FunctionalInterface
public interface ToolCaller {
    ToolReply call(AiTool tool) throws ValidateException, InterruptedException;
}
