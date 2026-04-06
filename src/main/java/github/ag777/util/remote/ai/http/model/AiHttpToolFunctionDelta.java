package github.ag777.util.remote.ai.http.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 工具函数流式增量模型。
 * 
 * <p>用于在流式响应中表示工具函数的增量信息，包含函数名称和参数的增量数据。
 * 与{@link AiHttpToolFunction}不同，该类只包含流式传输过程中的增量部分。
 * 
 * <p>通常在流式响应中，函数名可能一次性传输完成，而参数可能分多次传输。
 * 
 * @author ag777
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AiHttpToolFunctionDelta {
    private String name;
    private String argumentsDelta;

    /**
     * 检查是否有函数名称。
     * 
     * @return 如果函数名称不为null且不为空返回true，否则返回false
     */
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    /**
     * 检查是否有参数增量。
     * 
     * @return 如果参数增量不为null且不为空返回true，否则返回false
     */
    public boolean hasArgumentsDelta() {
        return argumentsDelta != null && !argumentsDelta.isEmpty();
    }
}
