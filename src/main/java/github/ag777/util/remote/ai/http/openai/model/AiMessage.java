package github.ag777.util.remote.ai.http.openai.model;

import com.google.gson.annotations.SerializedName;
import github.ag777.util.remote.ai.http.model.AiHttpToolCall;
import github.ag777.util.remote.ai.http.openai.model.request.RequestToolCall;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午5:32
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AiMessage {

    /**
     * 消息的角色
     */
    private String role;

    /**
     * 消息的内容。
     *
     * <p>纯文本消息使用 {@link String}；多模态消息使用 {@link List}&lt;{@link AiMessageContentPart}&gt;。
     */
    private Object content;

    /**
     * 助手消息携带的工具调用列表。
     *
     * <p>仅在 role 为 assistant 且模型发起了工具调用时存在，序列化为 OpenAI 协议的 {@code tool_calls}。
     */
    @SerializedName("tool_calls")
    private List<RequestToolCall> toolCalls;

    /**
     * 工具结果消息对应的工具调用ID。
     *
     * <p>仅在 role 为 tool 时存在，序列化为 OpenAI 协议的 {@code tool_call_id}。
     */
    @SerializedName("tool_call_id")
    private String toolCallId;

    /**
     * 创建仅含角色与内容的消息。
     *
     * @param role 消息角色
     * @param content 消息内容
     */
    public AiMessage(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 生成用户角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static AiMessage user(String content) {
        return new AiMessage(AiRoles.USER, content);
    }

    /**
     * 生成用户角色多模态消息。
     *
     * @param contentParts 消息内容片段
     * @return 生成的消息对象
     */
    public static AiMessage user(List<AiMessageContentPart> contentParts) {
        return ofParts(AiRoles.USER, contentParts);
    }

    /**
     * 生成助手角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static AiMessage assistant(String content) {
        return new AiMessage(AiRoles.ASSISTANT, content);
    }

    /**
     * 生成系统角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static AiMessage system(String content) {
        return new AiMessage(AiRoles.SYSTEM, content);
    }

    /**
     * 根据角色和内容生成消息对象
     *
     * @param role   消息的角色
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static AiMessage of(String role, String content) {
        return new AiMessage(role, content);
    }

    /**
     * 根据角色和多模态内容生成消息对象。
     *
     * @param role 消息的角色
     * @param contentParts 消息内容片段
     * @return 生成的消息对象
     */
    public static AiMessage ofParts(String role, List<AiMessageContentPart> contentParts) {
        return new AiMessage(role, contentParts);
    }

    /**
     * 生成携带工具调用的助手消息。
     *
     * <p>用于把模型返回的工具调用写回对话历史，是工具调用闭环的关键一步。
     *
     * @param content 助手文本内容，可为null
     * @param toolCalls 模型返回的工具调用列表
     * @return 生成的消息对象
     */
    public static AiMessage assistantToolCalls(String content, List<AiHttpToolCall> toolCalls) {
        AiMessage message = new AiMessage(AiRoles.ASSISTANT, content);
        if (toolCalls != null && !toolCalls.isEmpty()) {
            List<RequestToolCall> requestToolCalls = new ArrayList<>(toolCalls.size());
            for (AiHttpToolCall toolCall : toolCalls) {
                RequestToolCall requestToolCall = RequestToolCall.of(toolCall);
                if (requestToolCall != null) {
                    requestToolCalls.add(requestToolCall);
                }
            }
            message.toolCalls = requestToolCalls;
        }
        return message;
    }

    /**
     * 生成工具结果消息。
     *
     * <p>把工具执行结果回传给模型，序列化为 OpenAI 协议的 {@code role=tool} 消息。
     *
     * @param toolCallId 对应的工具调用ID
     * @param content 工具执行结果内容
     * @return 生成的消息对象
     */
    public static AiMessage tool(String toolCallId, String content) {
        AiMessage message = new AiMessage(AiRoles.TOOL, content);
        message.toolCallId = toolCallId;
        return message;
    }
}
