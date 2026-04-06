package github.ag777.util.remote.ai.http.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具函数最终态模型。
 * 
 * <p>表示工具函数的完整信息，包含函数名称、参数文本和解析后的参数Map。
 * 与{@link AiHttpToolFunctionDelta}不同，该类包含函数的最终完整数据。
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
public class AiHttpToolFunction {
    private String name;
    private String argumentsText;
    private Map<String, Object> arguments = new LinkedHashMap<>();

    /**
     * 检查是否有函数名称。
     * 
     * @return 如果函数名称不为null且不为空返回true，否则返回false
     */
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    /**
     * 检查是否有参数文本。
     * 
     * @return 如果参数文本不为null且不为空返回true，否则返回false
     */
    public boolean hasArgumentsText() {
        return argumentsText != null && !argumentsText.isEmpty();
    }

    /**
     * 检查是否有解析后的参数。
     * 
     * @return 如果参数Map不为null且不为空返回true，否则返回false
     */
    public boolean hasArguments() {
        return arguments != null && !arguments.isEmpty();
    }
}
