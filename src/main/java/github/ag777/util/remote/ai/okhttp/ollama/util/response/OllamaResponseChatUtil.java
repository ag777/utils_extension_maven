package github.ag777.util.remote.ai.okhttp.ollama.util.response;

import com.ag777.util.gson.GsonUtils;
import com.ag777.util.gson.JsonObjectUtils;
import com.ag777.util.lang.exception.model.JsonSyntaxException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.ag777.util.remote.ai.okhttp.ollama.model.OllamaToolCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * @version 2024/12/5 上午10:37
 */
public class OllamaResponseChatUtil extends OllamaResponseBaseUtil {
    
    /**
     * 构造函数
     * @param jo Ollama API返回的JSON响应对象
     */
    public OllamaResponseChatUtil(JsonObject jo) {
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
        JsonObject messages = getMessages();
        return JsonObjectUtils.getStr(messages, "content");
    }

    /**
     * 获取工具调用列表
     * 解析响应中的tool_calls字段，转换为OllamaToolCall对象列表
     * 
     * @return 工具调用列表，如果没有工具调用则返回空列表
     * @throws JsonSyntaxException 当JSON解析出错时抛出
     */
    public List<OllamaToolCall> getToolCalls() throws JsonSyntaxException {
        JsonObject messages = getMessages();
        JsonElement temp = messages.get("tool_calls");
        if (temp == null || temp.isJsonNull()) {
            return Collections.emptyList();
        }
        JsonArray ja;
        if (temp.isJsonArray()) {
            ja = temp.getAsJsonArray();
        } else {
            // 如果不是数组，创建只包含一个元素的数组
            ja = new JsonArray(1);
            ja.add(temp.getAsJsonObject());
        }
        List<OllamaToolCall> functions = new ArrayList<>(1);
        for (JsonElement je : ja) {
            JsonObject item = je.getAsJsonObject();
            for (String type : item.keySet()) {
                /*
                工具调用格式示例:
                {
                    "name": "get_current_weather",
                    "arguments": {
                        "format": "celsius",
                        "location": "福州"
                    }
                }
                */
                JsonObject jo = item.get(type).getAsJsonObject();
                OllamaToolCall func = new OllamaToolCall();
                func.setType(type);                    // 设置工具调用类型
                func.setName(JsonObjectUtils.getStr(jo, "name"));  // 设置函数名
                temp = jo.get("arguments");
                if (temp != null) {
                    // 解析函数参数为Map
                    Map<String, Object> arguments = GsonUtils.get().toMapWithException(temp.toString());
                    func.setArguments(arguments);
                }
                functions.add(func);
            }
        }
        return functions;
    }

    /**
     * 获取消息对象
     * 处理响应中的message字段，支持单个消息对象或消息数组
     * 
     * @return 消息的JsonObject对象
     */
    private JsonObject getMessages() {
        JsonElement je = JsonObjectUtils.get(data, "message");
        if (je.isJsonArray()) {
            // 如果是数组，返回第一个消息
            return je.getAsJsonArray().get(0).getAsJsonObject();
        } else {
            // 如果是单个对象，直接返回
            return je.getAsJsonObject();
        }
    }
}
