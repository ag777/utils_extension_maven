package github.ag777.util.remote.ai.http.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具调用流式增量模型。
 * 
 * <p>用于在流式响应中表示工具调用的增量信息，包含索引、ID、类型和函数增量。
 * 与{@link AiHttpToolCall}不同，该类只包含流式传输过程中的增量部分。
 * 
 * <p>在流式响应中，工具调用可能分多次传输，每次传输的部分信息通过该类表示。
 * 
 * @author ag777
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AiHttpToolCallDelta {
    private Integer index;
    private String id;
    private String type;
    private AiHttpToolFunctionDelta function;
    private final Map<String, Object> extra = new LinkedHashMap<>();

    /**
     * 检查是否有函数信息。
     * 
     * @return 如果函数信息不为null返回true，否则返回false
     */
    public boolean hasFunction() {
        return function != null;
    }

    /**
     * 检查是否为函数类型的工具调用。
     * 
     * @return 如果类型为null或"function"返回true，否则返回false
     */
    public boolean isFunctionCall() {
        return type == null || "function".equals(type);
    }
}
