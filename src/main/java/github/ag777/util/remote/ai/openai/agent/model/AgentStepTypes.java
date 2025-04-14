package github.ag777.util.remote.ai.openai.agent.model;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/9 上午10:50
 */
public class AgentStepTypes {
    /** 开始执行 */
    public static final String AGENT_MESSAGE="agent_message";
    
    /** 开始调用工具 */
    public static final String TOOL_START="tool_start";
    
    /** 工具调用结束 */
    public static final String TOOL_END="tool_finished";

    /** 对话结束 */
    public static final String MESSAGE_END ="message_end";
}
