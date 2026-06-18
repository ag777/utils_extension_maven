package github.ag777.util.remote.ai.http.openai.model.request;

import github.ag777.util.remote.ai.http.model.AiHttpToolCall;
import github.ag777.util.remote.ai.http.model.AiHttpToolFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 助手消息携带的工具调用请求体。
 *
 * <p>用于把模型返回的工具调用按 OpenAI 协议格式写回 messages，
 * 序列化后形如：
 * <pre>{@code
 * {"id":"call_xx","type":"function","function":{"name":"get_weather","arguments":"{\"city\":\"上海\"}"}}
 * }</pre>
 *
 * <p>该结构与 {@link AiHttpToolCall} 不同：{@link AiHttpToolCall} 是面向调用方的解析态，
 * 而该类是面向请求序列化的请求态，字段命名严格遵循 OpenAI 协议。
 *
 * @author ag777
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class RequestToolCall {
    private String id;
    private String type;
    private FunctionDTO function;

    /**
     * 由解析态的工具调用构建请求态工具调用。
     *
     * @param toolCall 解析得到的工具调用，可为null
     * @return 请求态工具调用，入参为null时返回null
     */
    public static RequestToolCall of(AiHttpToolCall toolCall) {
        if (toolCall == null) {
            return null;
        }
        RequestToolCall call = new RequestToolCall()
                .id(toolCall.id())
                .type(toolCall.type() == null ? "function" : toolCall.type());
        AiHttpToolFunction function = toolCall.function();
        if (function != null) {
            String arguments = function.argumentsText();
            if (arguments == null) {
                arguments = "";
            }
            call.function(new FunctionDTO(function.name(), arguments));
        }
        return call;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class FunctionDTO {
        private String name;
        private String arguments;
    }
}
