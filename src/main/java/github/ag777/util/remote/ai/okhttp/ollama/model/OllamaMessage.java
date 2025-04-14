package github.ag777.util.remote.ai.okhttp.ollama.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/12/4 下午5:32
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OllamaMessage {

    /**
     * 消息的角色
     */
    private String role;

    /**
     * 消息的内容
     */
    private String content;

    /**
     * 生成用户角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static OllamaMessage user(String content) {
        return new OllamaMessage(OllamaRoles.USER, content);
    }

    /**
     * 生成助手角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static OllamaMessage assistant(String content) {
        return new OllamaMessage(OllamaRoles.ASSISTANT, content);
    }
    
    /**
     * 生成系统角色消息
     *
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static OllamaMessage system(String content) {
        return new OllamaMessage(OllamaRoles.SYSTEM, content);
    }

    /**
     * 根据角色和内容生成消息对象
     *
     * @param role   消息的角色
     * @param content 消息的内容
     * @return 生成的消息对象
     */
    public static OllamaMessage of(String role, String content) {
        return new OllamaMessage(role, content);
    }
}
