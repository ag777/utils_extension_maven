package github.ag777.util.remote.ai.openai.agent.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/8 下午4:12
 */
@Data
@Accessors(chain = true)
public class ToolReply {
    private boolean success;
    private String message;
    private Object data;

    public static ToolReply success(Object data) {
        return new ToolReply().setSuccess(true).setData(data);
    }

    public static ToolReply fail(String errMsg) {
        return new ToolReply().setSuccess(false).setMessage(errMsg);
    }
}
