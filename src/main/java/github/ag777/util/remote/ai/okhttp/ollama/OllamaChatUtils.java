package github.ag777.util.remote.ai.okhttp.ollama;

import com.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.okhttp.ollama.interf.OnMessage;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaMessage;
import github.ag777.util.remote.ai.okhttp.ollama.model.request.OllamaRequestChat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/23 上午11:54
 */
public class OllamaChatUtils {

    public static String chatStream(OllamaApiClient client, String modelName, String query, List<OllamaMessage> history, Map<String, Object> options, OnMessage onMessage) throws ValidateException, IOException, InterruptedException {
        // 添加用户消息
        OllamaMessage userMsg = OllamaMessage.user(query);
        List<OllamaMessage> messages;
        if (history == null) {
            messages = List.of(
                    userMsg
            );
        } else {
            messages = history;
            messages.add(userMsg);
        }

        // 准备请求
        OllamaRequestChat request = OllamaRequestChat.of(modelName, options)
                .messages(messages);
        StringBuilder sb = new StringBuilder();
        // 发起流式聊天请求，并累积响应消息
        client.chatStream(request, (msg, tcs)->{
            sb.append(msg);
            onMessage.accept(msg, tcs);
        });
        return sb.toString();
    }
}
