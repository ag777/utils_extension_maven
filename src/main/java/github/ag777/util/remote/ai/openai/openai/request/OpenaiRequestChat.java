package github.ag777.util.remote.ai.openai.openai.request;

import com.google.gson.annotations.SerializedName;
import github.ag777.util.remote.ai.openai.model.request.RequestChat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 聊天请求类
 * 用于发送聊天相关的请求，支持多轮对话、工具调用和图片输入
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/04/03 下午15:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true, fluent = true)
public class OpenaiRequestChat extends RequestChat<OpenaiRequestChat> {
    private Float temperature;
    @SerializedName("top_p")
    private Float topP;
    public OpenaiRequestChat(String model) {
        super(model);
    }

    public static OpenaiRequestChat of(String model) {
        return new OpenaiRequestChat(model);
    }
}
