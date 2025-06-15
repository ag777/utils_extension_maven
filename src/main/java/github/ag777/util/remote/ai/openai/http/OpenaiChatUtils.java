package github.ag777.util.remote.ai.openai.http;

import github.ag777.util.lang.exception.model.ValidateException;
import github.ag777.util.remote.ai.openai.http.interf.OpenaiOnMessage;
import github.ag777.util.remote.ai.openai.http.request.OpenaiRequestChat;
import github.ag777.util.remote.ai.openai.model.AiMessage;

import java.io.IOException;
import java.util.List;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/23 上午11:54
 */
public class OpenaiChatUtils {

    public static String chatStream(OpenaiApiClient client, String modelName, String query, List<AiMessage> histories, Float temperature, Float topP, OpenaiOnMessage onMessage) throws ValidateException, InterruptedException, IOException {
        // 添加用户消息
        AiMessage userMsg = AiMessage.user(query);
        List<AiMessage> messages;
        if (histories == null) {
            messages = List.of(
                    userMsg
            );
        } else {
            messages = histories;
            messages.add(userMsg);
        }

        // 准备请求
        OpenaiRequestChat request = OpenaiRequestChat.of(modelName)
                .messages(messages);
        if (temperature != null) {
            request.temperature(temperature);
        }
        if (topP != null) {
            request.topP(topP);
        }
        StringBuilder sb = new StringBuilder();
        // 发起流式聊天请求，并累积响应消息
        client.chatStream(request, (msg, toolCalls, res)->{
            sb.append(msg);
            onMessage.accept(msg, toolCalls, res);
        });

        return sb.toString();
    }
}
