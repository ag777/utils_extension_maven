package github.ag777.util.remote.ai.openai.agent.model;

import github.ag777.util.remote.ai.openai.model.AiReply;
import github.ag777.util.remote.ai.openai.model.AiTool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/8 下午3:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentReply {
    private String reactPrompt;
    private AiReply reply;
    private AiTool tool;
}
