package github.ag777.util.remote.ai.http.config;

import github.ag777.util.http.HttpHelper;
import github.ag777.util.http.HttpUtils;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AI HTTP客户端配置类。
 * 
 * <p>用于配置AI HTTP客户端的各种参数，包括：
 * <ul>
 * <li>API基础URL和聊天接口路径</li>
 * <li>API密钥和认证配置</li>
 * <li>自定义HTTP头</li>
 * <li>HTTP助手和执行器配置</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * AiHttpClientConfig config = AiHttpClientConfig.create("https://api.openai.com")
 *     .apiKey("sk-xxx")
 *     .chatPath("/v1/chat/completions")
 *     .header("User-Agent", "MyApp/1.0");
 * </pre>
 * 
 * @author ag777
 * @since 1.0
 */
public class AiHttpClientConfig {
    private String baseUrl;
    private String chatPath = "/v1/chat/completions";
    private String apiKey;
    private String apiKeyHeader = "Authorization";
    private String apiKeyPrefix = "Bearer ";
    private final Map<String, Object> headers = new LinkedHashMap<>();
    private HttpHelper httpHelper;
    private Executor executor = Executors.newCachedThreadPool();

    /**
     * 创建指定基础URL的配置对象。
     * 
     * @param baseUrl API基础URL
     * @return 新的配置对象实例
     */
    public static AiHttpClientConfig create(String baseUrl) {
        return new AiHttpClientConfig().baseUrl(baseUrl);
    }

    /**
     * 获取基础URL。
     * 
     * @return 基础URL
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * 设置基础URL。
     * 
     * @param baseUrl 基础URL
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 获取聊天接口路径。
     * 
     * @return 聊天接口路径，默认为 "/v1/chat/completions"
     */
    public String chatPath() {
        return chatPath;
    }

    /**
     * 设置聊天接口路径。
     * 
     * @param chatPath 聊天接口路径
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig chatPath(String chatPath) {
        this.chatPath = chatPath;
        return this;
    }

    /**
     * 获取API密钥。
     * 
     * @return API密钥
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * 设置API密钥。
     * 
     * @param apiKey API密钥
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * 获取API密钥请求头名称。
     * 
     * @return API密钥请求头名称，默认为 "Authorization"
     */
    public String apiKeyHeader() {
        return apiKeyHeader;
    }

    /**
     * 设置API密钥请求头名称。
     * 
     * @param apiKeyHeader API密钥请求头名称
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig apiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
        return this;
    }

    /**
     * 获取API密钥前缀。
     * 
     * @return API密钥前缀，默认为 "Bearer "
     */
    public String apiKeyPrefix() {
        return apiKeyPrefix;
    }

    /**
     * 设置API密钥前缀。
     * 
     * @param apiKeyPrefix API密钥前缀
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig apiKeyPrefix(String apiKeyPrefix) {
        this.apiKeyPrefix = apiKeyPrefix;
        return this;
    }

    /**
     * 获取自定义请求头。
     * 
     * @return 自定义请求头Map
     */
    public Map<String, Object> headers() {
        return headers;
    }

    /**
     * 设置自定义请求头。
     * 
     * @param key 请求头名称
     * @param value 请求头值，如果为null则移除该请求头
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig header(String key, Object value) {
        if (value == null) {
            headers.remove(key);
        } else {
            headers.put(key, value);
        }
        return this;
    }

    /**
     * 获取HTTP助手。
     * 
     * <p>如果没有设置，会使用默认配置创建一个。
     * 
     * @return HTTP助手实例
     */
    public HttpHelper httpHelper() {
        if (httpHelper == null) {
            OkHttpClient client = HttpUtils.defaultBuilder()
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();
            httpHelper = new HttpHelper(client, null);
        }
        return httpHelper;
    }

    /**
     * 设置HTTP助手。
     * 
     * @param httpHelper HTTP助手实例
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig httpHelper(HttpHelper httpHelper) {
        this.httpHelper = httpHelper;
        return this;
    }

    /**
     * 获取异步执行器。
     * 
     * @return 异步执行器
     */
    public Executor executor() {
        return executor;
    }

    /**
     * 设置异步执行器。
     * 
     * @param executor 异步执行器
     * @return 当前配置对象，支持链式调用
     */
    public AiHttpClientConfig executor(Executor executor) {
        this.executor = executor;
        return this;
    }
}
