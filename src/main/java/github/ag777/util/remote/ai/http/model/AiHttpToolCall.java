package github.ag777.util.remote.ai.http.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具调用最终态模型。
 * 
 * <p>表示工具调用的完整信息，包含索引、ID、类型和完整的函数信息。
 * 与{@link AiHttpToolCallDelta}不同，该类包含工具调用的最终完整数据。
 * 
 * <p>在流式响应完成后，所有的增量数据会被组装成该最终态对象。
 * 
 * @author ag777
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AiHttpToolCall {
    private Integer index;
    private String id;
    private String type = "function";
    private AiHttpToolFunction function;
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
