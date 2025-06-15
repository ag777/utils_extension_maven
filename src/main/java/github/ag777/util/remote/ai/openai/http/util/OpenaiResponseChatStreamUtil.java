package github.ag777.util.remote.ai.openai.http.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.ag777.util.gson.JsonObjectUtils;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.remote.ai.openai.model.response.ResponseBaseUtil;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Ollama聊天响应解析工具类
 * 用于解析Ollama API的聊天响应，包括消息内容和工具调用
 * 
 * 响应示例:
 * {
 *     "model": "qwen2.5:14b",
 *     "created_at": "2024-12-05T02:18:50.646342613Z",
 *     "message": {
 *         "role": "assistant",
 *         "content": "",
 *         "tool_calls": [
 *             {
 *                 "function": {
 *                     "name": "get_current_weather",
 *                     "arguments": {
 *                         "format": "celsius",
 *                         "location": "福州"
 *                     }
 *                 }
 *             }
 *         ]
 *     },
 *     "done_reason": "stop",
 *     "done": true,
 *     "total_duration": 1343237273,
 *     "load_duration": 22134117,
 *     "prompt_eval_count": 223,
 *     "prompt_eval_duration": 52000000,
 *     "eval_count": 62,
 *     "eval_duration": 1256000000
 * }
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/3/24 上午10:20
 */
public class OpenaiResponseChatStreamUtil extends ResponseBaseUtil {

    /**
     * 构造函数
     * @param jo Ollama API返回的JSON响应对象
     */
    public OpenaiResponseChatStreamUtil(JsonObject jo) {
        super(jo);
    }

    /**
     * 获取聊天消息内容
     * 从响应中提取message.content字段的值
     * 
     * @return 聊天消息文本内容
     * @throws GsonSyntaxException 当JSON解析出错时抛出
     */
    public Content getMessage() throws GsonSyntaxException {
        /*
        choices -> {JsonArray@3458} "[{"index":0,"delta":{"content":null,"reasoning_content":"好的","role":"assistant"},"finish_reason":null}]"
         */
        JsonArray choices = JsonObjectUtils.getJsonArray(data, "choices");
        if (choices != null && choices.isEmpty()) {
            return null;
        }
        String finishReason = JsonObjectUtils.getStr(choices.get(0).getAsJsonObject(), "finish_reason");
        JsonObject delta = JsonObjectUtils.getJsonObject(choices.get(0).getAsJsonObject(), "delta");
        if (delta == null) {
            return null;
        }
        String thinking = null;
        String content = JsonObjectUtils.getStr(delta, "content");
        if (content == null) {
            thinking = JsonObjectUtils.getStr(delta, "reasoning_content");
        }
        ToolCall toolCall=null;
        if (thinking == null) {
            JsonArray arr = JsonObjectUtils.getJsonArray(delta, "tool_call");
            if (arr != null) {
                /*
                [{
                    "index": 0,
                    "delta": {
                        "tool_calls": [{
                            "index": 0,
                            "function": {
                                "arguments": "{\""
                            }
                        }]
                    },
                    "logprobs": null,
                    "finish_reason": null
                }]
                 */
                JsonObject function = JsonObjectUtils.getJsonObject(arr.get(0).getAsJsonObject(), "function");

                toolCall=new ToolCall()
                        .setName(JsonObjectUtils.getStr(function, "name"))
                        .setArguments(JsonObjectUtils.getStr(function, "arguments"));
            }
        }
        return new Content(content, thinking, toolCall, finishReason);
    }

    public record Content(String content, String thinking, ToolCall toolCall, String finishReason) {}
    
    @Data
    @Accessors(chain = true)
    public static class ToolCall {
        private String name;
        private String arguments;
    }
}
