package github.ag777.util.remote.ai.http;

import github.ag777.util.remote.ai.http.client.AiHttpChatSession;
import github.ag777.util.remote.ai.http.client.AiHttpClient;
import github.ag777.util.remote.ai.http.config.AiHttpClientConfig;

/**
 * AI HTTP客户端工具入口。
 * 
 * <p>提供创建各种AI HTTP客户端和会话的便捷方法，支持OpenAI兼容的API。
 * 
 * <p>主要功能：
 * <ul>
 * <li>创建OpenAI兼容的HTTP客户端</li>
 * <li>创建预设配置的聊天会话</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * // 创建客户端
 * AiHttpClient client = AiHttpClients.openAiCompatible("https://api.openai.com", "sk-xxx");
 * 
 * // 创建会话
 * AiHttpChatSession session = AiHttpClients.openAiCompatibleSession(
 *     "https://api.openai.com", "sk-xxx", "gpt-3.5-turbo");
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
public class AiHttpClients {
    private AiHttpClients() {
    }

    /**
     * 创建OpenAI兼容的AI HTTP客户端。
     * 
     * @param baseUrl API基础URL，如 "https://api.openai.com"
     * @param apiKey API密钥，如 "sk-xxx"
     * @return 配置完成的AI HTTP客户端
     * @throws IllegalArgumentException 如果baseUrl或apiKey为null或空
     */
    public static AiHttpClient openAiCompatible(String baseUrl, String apiKey) {
        return AiHttpClient.openAiCompatible(
                AiHttpClientConfig.create(baseUrl)
                        .apiKey(apiKey)
        );
    }

    /**
     * 创建OpenAI兼容的AI聊天会话。
     * 
     * <p>该方法会自动创建客户端并设置模型，返回一个可直接用于对话的会话对象。
     * 
     * @param baseUrl API基础URL，如 "https://api.openai.com"
     * @param apiKey API密钥，如 "sk-xxx"
     * @param model 模型名称，如 "gpt-3.5-turbo"
     * @return 配置完成的AI聊天会话
     * @throws IllegalArgumentException 如果任意参数为null或空
     */
    public static AiHttpChatSession openAiCompatibleSession(String baseUrl, String apiKey, String model) {
        return AiHttpChatSession.of(openAiCompatible(baseUrl, apiKey), model);
    }
}
