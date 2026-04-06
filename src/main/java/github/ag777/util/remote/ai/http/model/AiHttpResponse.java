package github.ag777.util.remote.ai.http.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一AI响应模型。
 * 
 * <p>表示AI模型的完整响应信息，包含对话内容、推理内容、工具调用、
 * 完成原因和原始响应数据。
 * 
 * <p>该类是所有AI服务响应的统一表示，无论使用何种协议或服务。
 * 
 * @author ag777
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AiHttpResponse {
    private String content;
    private String reasoning;
    private List<AiHttpToolCall> toolCalls = new ArrayList<>();
    private String finishReason;
    private JsonObject raw;
}
