package github.ag777.util.remote.ai.openai.agent.model;

import com.ag777.util.lang.exception.ExceptionUtils;
import github.ag777.util.remote.ai.openai.model.AiTool;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AI代理执行步骤信息类
 * 用于记录和表示AI代理执行过程中的各种事件和状态信息
 * 
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/9 上午10:50
 */
@Data
@Accessors(chain = true)
public class AgentStepInfo {
    /**
     * 表示步骤执行成功的状态码
     */
    public static final int STATUS_SUCCESS = 0;

    /**
     * 事件类型，表示当前步骤的类型，参考{@link AgentStepTypes}
     */
    private String event;
    
    /**
     * 状态码，0表示成功，非0表示失败
     */
    private int status;
    
    /**
     * 消息内容，可以是步骤的描述信息或错误信息
     */
    private String message;
    
    /**
     * 关联的AI工具，当事件类型与工具相关时使用
     */
    private AiTool tool;

    /**
     * 工具的返回
     */
    private Object toolReply;

    /**
     * 异常信息
     */
    private String err;

    /**
     * 构造函数
     * 
     * @param event 事件类型
     */
    public AgentStepInfo(String event) {
        this.event = event;
        this.status = STATUS_SUCCESS;
    }

    /**
     * 创建一个消息类型的步骤信息
     * 
     * @param message 消息内容
     * @return 步骤信息实例
     */
    public static AgentStepInfo message(String message) {
        return new AgentStepInfo(AgentStepTypes.AGENT_MESSAGE).setMessage(message);
    }

    /**
     * 创建一个工具开始执行的步骤信息
     * 
     * @param tool 要执行的AI工具
     * @return 步骤信息实例
     */
    public static AgentStepInfo toolStart(AiTool tool) {
        return new AgentStepInfo(AgentStepTypes.TOOL_START).setTool(tool);
    }

    /**
     * 创建一个工具执行结束的步骤信息
     * 
     * @param tool 执行完成的AI工具
     * @return 步骤信息实例
     */
    public static AgentStepInfo toolEnd(AiTool tool, Object toolReply) {
        return new AgentStepInfo(AgentStepTypes.TOOL_END).setTool(tool).setToolReply(toolReply);
    }

    /**
     * 创建一个成功结束的步骤信息
     * 
     * @return 步骤信息实例
     */
    public static AgentStepInfo end() {
        return new AgentStepInfo(AgentStepTypes.MESSAGE_END);
    }

    /**
     * 创建一个失败结束的步骤信息
     * 
     * @param errMsg 错误信息
     * @return 步骤信息实例
     */
    public static AgentStepInfo endFail(String errMsg) {
        return new AgentStepInfo(AgentStepTypes.MESSAGE_END).setStatus(100).setMessage(errMsg);
    }

    /**
     * 创建一个失败结束的步骤信息
     * @param errMsg 错误信息
     * @param e 异常
     * @param packageName 包名
     * @return 步骤信息实例
     */
    public static AgentStepInfo endFail(String errMsg, Throwable e, String packageName) {
        AgentStepInfo info = endFail(errMsg);
        if (e != null) {
            info.setErr(ExceptionUtils.getErrMsg(e, packageName));
        }
        return info;
    }
}
