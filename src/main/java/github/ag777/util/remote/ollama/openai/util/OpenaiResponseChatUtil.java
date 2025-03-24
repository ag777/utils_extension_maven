package github.ag777.util.remote.ollama.openai.util;

import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
public class OpenaiResponseChatUtil extends OpenaiResponseBaseUtil {

    /**
     * 构造函数
     * @param jo Ollama API返回的JSON响应对象
     */
    public OpenaiResponseChatUtil(JsonObject jo) {
        super(jo);
    }

    /**
     * 获取聊天消息内容
     * 从响应中提取message.content字段的值
     * 
     * @return 聊天消息文本内容
     * @throws JsonSyntaxException 当JSON解析出错时抛出
     */
    public String getMessage() throws JsonSyntaxException {
        /*
        {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-3.5-turbo-0613", "system_fingerprint": "fp_44709d6fcb", "choices":[{"index":0,"delta":{"role":"assistant","content":""},"finish_reason":null}]}
         */
        JsonArray choices = JsonObjectUtils.getJsonArray(data, "choices");
        if (choices.isEmpty()) {
            return "";
        }
        JsonObject delta = JsonObjectUtils.getJsonObject(choices.get(0).getAsJsonObject(), "delta");
        if (delta == null) {
            return "";
        }
        return JsonObjectUtils.getStr(delta, "content", "");
    }




}
