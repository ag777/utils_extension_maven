package github.ag777.util.remote.ai.openai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/7 下午5:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AiTool {
    private String name;
    private Map<String, Object> params;
}
