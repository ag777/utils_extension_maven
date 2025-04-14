package github.ag777.util.remote.ai.openai.agent.model;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/4/8 下午4:54
 */
@FunctionalInterface
public interface OnAgentMsg {
    void accept(AgentStepInfo stepInfo) throws InterruptedException;
}
