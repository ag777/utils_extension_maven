package github.ag777.util.remote.ai.http.provider;

import com.google.gson.JsonObject;
import github.ag777.util.lang.exception.model.GsonSyntaxException;
import github.ag777.util.remote.ai.http.model.AiHttpChunk;
import github.ag777.util.remote.ai.http.model.AiHttpRequest;
import github.ag777.util.remote.ai.http.model.AiHttpResponse;

import java.util.Map;

/**
 * AI HTTP协议适配器接口。
 * 
 * <p>定义了AI HTTP客户端的协议适配规范，负责：
 * <ul>
 * <li>将统一请求对象转换为特定协议的请求体</li>
 * <li>解析特定协议的响应为统一响应对象</li>
 * <li>解析流式响应分片</li>
 * </ul>
 * 
 * <p>通过实现该接口，可以支持不同的AI服务协议，如OpenAI、Claude等。
 * 
 * @author ag777
 * @since 1.0
 */
public interface AiHttpProvider {

    /**
     * 构建特定协议的请求体。
     * 
     * @param request 统一请求对象
     * @param stream 是否为流式请求
     * @return 符合特定协议格式的请求体Map
     */
    Map<String, Object> buildRequestBody(AiHttpRequest request, boolean stream);

    /**
     * 解析特定协议的非流式响应。
     * 
     * @param json 响应JSON对象
     * @return 解析后的统一响应对象
     * @throws GsonSyntaxException JSON解析异常
     */
    AiHttpResponse parseResponse(JsonObject json) throws GsonSyntaxException;

    /**
     * 解析特定协议的流式响应分片。
     * 
     * @param json 流式分片JSON对象
     * @return 解析后的分片对象
     * @throws GsonSyntaxException JSON解析异常
     */
    AiHttpChunk parseStreamChunk(JsonObject json) throws GsonSyntaxException;
}
