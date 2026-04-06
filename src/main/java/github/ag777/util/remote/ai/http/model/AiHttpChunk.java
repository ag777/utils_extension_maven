package github.ag777.util.remote.ai.http.model;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 流式响应分片模型。
 * 
 * <p>表示流式响应中的单个分片，包含内容增量、推理增量、工具调用增量、
 * 完成原因和原始分片数据。
 * 
 * <p>在流式响应过程中，AI模型会分多次发送数据，每次发送的部分通过该类表示。
 * 所有的分片累积起来形成完整的响应。
 * 
 * @author ag777
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AiHttpChunk {
    private String content;
    private String reasoning;
    private List<AiHttpToolCallDelta> toolCalls;
    private String finishReason;
    private JsonObject raw;

    /**
     * 检查是否有内容增量。
     * 
     * @return 如果内容增量不为null且不为空返回true，否则返回false
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    /**
     * 检查是否有推理增量。
     * 
     * @return 如果推理增量不为null且不为空返回true，否则返回false
     */
    public boolean hasReasoning() {
        return reasoning != null && !reasoning.isEmpty();
    }

    /**
     * 检查是否有工具调用增量。
     * 
     * @return 如果工具调用增量不为null且不为空返回true，否则返回false
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    /**
     * 检查是否已完成。
     * 
     * @return 如果完成原因不为null且不为空返回true，否则返回false
     */
    public boolean isFinished() {
        return finishReason != null && !finishReason.isEmpty();
    }
}
