package github.ag777.util.remote.ollama.okhttp.interf;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ollama.okhttp.model.OllamaToolCall;

import java.util.List;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/23 上午9:36
 */
@FunctionalInterface
public interface OnMessage {
    void accept(String message, List<OllamaToolCall> toolCalls) throws ValidateException, InterruptedException;
}
