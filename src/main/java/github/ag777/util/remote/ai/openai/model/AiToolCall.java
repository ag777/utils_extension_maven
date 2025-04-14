package github.ag777.util.remote.ai.openai.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/5 下午2:38
 */
@NoArgsConstructor
@Data
public class AiToolCall {
    private String type;
    private String name;
    private Map<String, Object> arguments;

    @Override
    public String toString() {
        return "["+name+"] "+arguments;
    }
}
